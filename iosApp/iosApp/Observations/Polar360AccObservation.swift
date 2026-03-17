//
//  Polar360AccObservation.swift
//  iosApp
//

import Combine
import CoreBluetooth
import Foundation
import KMPNativeCoroutinesCombine
import PolarBleSdk
import RxSwift
import shared

class Polar360AccObservation: Observation_ {

    private let deviceIdentificer: Set<String> = ["Polar", "360"]
    private let bleManager = BluetoothStateManagement.shared
    private let controller = Polar360Controller.shared

    private var accDisposable: Disposable?
    private var offlineRecordingDisposable: Disposable?
    private var deviceListener: AnyCancellable?
    private var offlineMode = false

    private static let notificationBackoffInterval: TimeInterval = 60
    private static var lastCannotStartNotificationDate: Date?

    init(repos: MainRepository, sensorPermissions: Set<String>) {
        super.init(repos: repos, observationType: Polar360AccType(sensorPermissions: sensorPermissions))
    }

    override func start() -> Bool {
        if !observerAccessible() {
            showCannotStartNotification()
            return false
        }

        guard let device = controller.findPolar360Device() else {
            showCannotStartNotification()
            return false
        }
        let deviceId = device.deviceId

        listenToDeviceConnection()

        controller.ensureReady(deviceId: deviceId, offlineMode: offlineMode).subscribe(
            onCompleted: { [weak self] in
                guard let self else { return }
                if self.offlineMode {
                    self.controller.stopOfflineRecordingAndFetch(
                        dataType: .acc,
                        onSuccess: { [weak self] items in
                            guard let self else { return }
                            let samples = items.compactMap { $0 as? Polar360Controller.acc_data }
                            if !samples.isEmpty {
                                let processed = samples.map { ["x": $0.x, "y": $0.y, "z": $0.z, "timestamp": $0.timestamp] as [String: Any] }
                                self.storeData(data: ["polar360accdata": processed], timestamp: -1) {}
                            }
                            self.offlineRecordingDisposable = self.controller.startOfflineRecording(
                                deviceId: deviceId, dataType: .acc
                            )
                        },
                        onError: { [weak self] error in
                            NSLog("Polar360AccObservation: Failed to fetch pending offline data: \(error)")
                            guard let self else { return }
                            self.offlineRecordingDisposable = self.controller.startOfflineRecording(
                                deviceId: deviceId, dataType: .acc
                            )
                        }
                    )
                } else {
                    let schedulerB = ConcurrentDispatchQueueScheduler(qos: .background)

                    self.accDisposable = self.controller.getPolarApi()
                        .requestStreamSettings(deviceId, feature: .acc)
                        .catch { error -> Single<PolarSensorSetting> in
                            print("Polar360 ACC settings request failed: \(error)")
                            let defaultSettings = try! PolarSensorSetting([
                                .sampleRate: 52,
                                .resolution: 1
                            ])
                            return Single.just(defaultSettings)
                        }
                        .asObservable()
                        .flatMap { settings in
                            return self.controller.getPolarApi()
                                .startAccStreaming(deviceId, settings: settings)
                        }
                        .subscribe(on: MainScheduler.instance)
                        .observe(on: schedulerB)
                        .subscribe(
                            onNext: { [weak self] data in
                                guard let self, let sample = data.first else { return }
                                self.storeData(
                                    data: [
                                        "x": sample.x,
                                        "y": sample.y,
                                        "z": sample.z,
                                        "timestamp": sample.timeStamp
                                    ],
                                    timestamp: -1
                                ) { }
                            },
                            onError: { error in
                                print("Polar360 Accelerometer stream failed: \(error)")
                            }
                        )
                }
            },
            onError: { error in print("Polar360 ACC setup error: \(error)") }
        )
        return true
    }

    override func stop(onCompletion: @escaping () -> Void) {
        deviceListener?.cancel()
        deviceListener = nil
        if offlineMode {
            offlineRecordingDisposable?.dispose()
            offlineRecordingDisposable = nil
            controller.stopOfflineRecordingAndFetch(
                dataType: .acc,
                onSuccess: { [weak self] items in
                    guard let self else { onCompletion(); return }
                    let samples = items.compactMap { $0 as? Polar360Controller.acc_data }
                    let processed = samples.map { ["x": $0.x, "y": $0.y, "z": $0.z, "timestamp": $0.timestamp] as [String: Any] }
                    self.storeData(data: ["polar360accdata": processed], timestamp: -1) { onCompletion() }
                },
                onError: { error in NSLog("Polar360AccObservation: Failed to fetch offline data: \(error)") }
            )
        } else {
            accDisposable?.dispose()
            accDisposable = nil
            onCompletion()
        }
    }

    override func observerErrors() -> Set<String> {
        var errors: Set<String> = []
        if CBManager.authorization != .allowedAlways {
            errors.insert("Access to Bluetooth not granted")
            PermissionManager.openSensorPermissionDialog()
        }
        if !bleManager.bluetoothActiveValue {
            errors.insert("Bluetooth is not enabled")
        }
        if !AppDelegate.shared.bluetoothController.observerDeviceAccessible(bleDevices: deviceIdentificer) {
            errors.insert("No polar device connected")
            errors.insert(Observation_.companion.ERROR_DEVICE_NOT_CONNECTED)
        }
        return errors
    }

    override func applyObservationConfig(settings: Dictionary<String, Any>) {
        offlineMode = controller.isOfflineRecordingMode(config: settings)
    }

    override func bleDevicesNeeded() -> Set<String> { deviceIdentificer }

    override func ableToAutomaticallyStart() -> Bool { false }

    private func showCannotStartNotification() {
        let now = Date()
        if let last = Polar360AccObservation.lastCannotStartNotificationDate,
           now.timeIntervalSince(last) < Polar360AccObservation.notificationBackoffInterval {
            return
        }
        Polar360AccObservation.lastCannotStartNotificationDate = now
        showObservationErrorNotification(
            notificationBody: "Cannot start Accelerometer Observation! Please enable Bluetooth and connect devices.",
            fallbackTitle: "Observation Error"
        )
    }

    private func listenToDeviceConnection() {
        deviceListener = createPublisher(for: bleManager.connectedDevices)
            .removeDuplicates()
            .receive(on: DispatchQueue.main)
            .sink(receiveCompletion: { _ in }, receiveValue: { [weak self] devices in
                if let self, !self.deviceIdentificer.anyNameIn(items: devices) {
                    self.controller.onDeviceDisconnected()
                    self.deviceListener?.cancel()
                }
            })
    }
}
