//
//  Polar360TempObservation.swift
//  iosApp
//

import Combine
import CoreBluetooth
import Foundation
import KMPNativeCoroutinesCombine
import PolarBleSdk
import RxSwift
import shared

class Polar360TempObservation: Observation_ {

    private let deviceIdentificer: Set<String> = ["Polar", "360"]
    private let bleManager = BluetoothStateManagement.shared
    private let controller = Polar360Controller.shared

    private var tempDisposable: Disposable?
    private var offlineRecordingDisposable: Disposable?
    private var deviceListener: AnyCancellable?
    private var offlineMode = false

    private static let notificationBackoffInterval: TimeInterval = 60
    private static var lastCannotStartNotificationDate: Date?

    init(repos: MainRepository, sensorPermissions: Set<String>) {
        super.init(repos: repos, observationType: Polar360TempType(sensorPermissions: sensorPermissions))
    }

    override func start() -> Bool {
        if !observerAccessible() {
            showCannotStartNotification()
            return false
        }

        guard let device = controller.findPolar360Device(),
              let deviceId = device.deviceId else {
            showCannotStartNotification()
            return false
        }

        listenToDeviceConnection()

        controller.ensureReady(deviceId: deviceId, offlineMode: offlineMode).subscribe(
            onCompleted: { [weak self] in
                guard let self else { return }
                if self.offlineMode {
                    self.offlineRecordingDisposable = self.controller.startOfflineRecording(
                        deviceId: deviceId, dataType: .temperature
                    )
                } else {
                    let schedulerB = ConcurrentDispatchQueueScheduler(qos: .background)

                    self.tempDisposable = self.controller.getPolarApi()
                        .requestStreamSettings(deviceId, feature: .temperature)
                        .catch { error -> Single<PolarSensorSetting> in
                            print("Polar360 Temp settings request failed: \(error)")
                            let defaultSettings = try! PolarSensorSetting([
                                .sampleRate: 1,
                                .resolution: 1
                            ])
                            return Single.just(defaultSettings)
                        }
                        .asObservable()
                        .flatMap { settings in
                            return self.controller.getPolarApi()
                                .startTemperatureStreaming(deviceId, settings: settings)
                        }
                        .subscribe(on: MainScheduler.instance)
                        .observe(on: schedulerB)
                        .subscribe(
                            onNext: { [weak self] data in
                                guard let self else { return }
                                if let sample = data.samples.first {
                                    self.storeData(
                                        data: [
                                            "temperature": sample.temperature,
                                            "timestamp": sample.timeStamp
                                        ],
                                        timestamp: -1
                                    ) { }
                                }
                            },
                            onError: { error in
                                print("Polar360 Temperature stream failed: \(error)")
                            }
                        )
                }
            },
            onError: { error in print("Polar360 Temp setup error: \(error)") }
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
                dataType: .temperature,
                onSuccess: { [weak self] items in
                    guard let self else { onCompletion(); return }
                    let samples = items.compactMap { $0 as? PolarTemperatureData.PolarTemperatureDataSample }
                    let processed = samples.map { ["temp": $0.temperature, "timestamp": $0.timeStamp] as [String: Any] }
                    self.storeData(data: ["polar360tempdata": processed], timestamp: -1) { onCompletion() }
                },
                onError: { error in
                    NSLog("Polar360TempObservation: Failed to fetch offline data: \(error)")
                    onCompletion()
                }
            )
        } else {
            tempDisposable?.dispose()
            tempDisposable = nil
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
        if let last = Polar360TempObservation.lastCannotStartNotificationDate,
           now.timeIntervalSince(last) < Polar360TempObservation.notificationBackoffInterval {
            return
        }
        Polar360TempObservation.lastCannotStartNotificationDate = now
        showObservationErrorNotification(
            notificationBody: "Cannot start Temperature Observation! Please enable Bluetooth and connect devices.",
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
