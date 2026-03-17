//
//  Polar360HrObservation.swift
//  iosApp
//

import Combine
import CoreBluetooth
import Foundation
import KMPNativeCoroutinesCombine
import PolarBleSdk
import RxSwift
import shared

class Polar360HrObservation: Observation_ {

    private let deviceIdentificer: Set<String> = ["Polar", "360"]
    private let bleManager = BluetoothStateManagement.shared
    private let controller = Polar360Controller.shared

    private var hrDisposable: Disposable?
    private var offlineRecordingDisposable: Disposable?
    private var deviceListener: AnyCancellable?
    private var offlineMode = false

    private static let notificationBackoffInterval: TimeInterval = 60
    private static var lastCannotStartNotificationDate: Date?

    init(repos: MainRepository, sensorPermissions: Set<String>) {
        super.init(repos: repos, observationType: Polar360HrType(sensorPermissions: sensorPermissions))
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
                    self.offlineRecordingDisposable = self.controller.startOfflineRecording(
                        deviceId: deviceId, dataType: .ppi
                    )
                } else {
                    self.hrDisposable = self.controller.getPolarApi()
                        .startHrStreaming(deviceId)
                        .do(onSubscribe: { print("Polar360 HR stream subscribing...") })
                        .catch { error in
                            print("Polar360 HR stream failed to start: \(error)")
                            return Observable.empty()
                        }
                        .subscribe(on: MainScheduler.instance)
                        .observe(on: ConcurrentDispatchQueueScheduler(qos: .userInitiated))
                        .subscribe(
                            onNext: { [weak self] data in
                                guard let self else { return }
                                for hrSample in data {
                                    self.storeData(data: ["hr": hrSample.hr], timestamp: -1) { }
                                }
                            },
                            onError: { [weak self] error in
                                print("Polar360 HR error: \(error)")
                                if let self {
                                    self.showCannotStartNotification()
                                    Observation_.pauseObservation(self.observationType)
                                }
                            }
                        )
                }
            },
            onError: { error in print("Polar360 HR setup error: \(error)") }
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
                dataType: .ppi,
                onSuccess: { [weak self] items in
                    guard let self else { onCompletion(); return }
                    let samples = items.compactMap { $0 as? Polar360Controller.hr_data }
                    let processed = samples.map { ["hr": $0.hr, "ts": $0.timestamp] as [String: Any] }
                    self.storeData(data: ["polar360hrdata": processed], timestamp: -1) { onCompletion() }
                },
                onError: { error in NSLog("Polar360HrObservation: Failed to fetch offline data: \(error)") }
            )
        } else {
            hrDisposable?.dispose()
            hrDisposable = nil
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
        } else if !PolarStates.shared.hrFeatureReadyValue {
            errors.insert("Heart-rate measurement feature unavailable")
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
        if let last = Polar360HrObservation.lastCannotStartNotificationDate,
           now.timeIntervalSince(last) < Polar360HrObservation.notificationBackoffInterval {
            return
        }
        Polar360HrObservation.lastCannotStartNotificationDate = now
        showObservationErrorNotification(
            notificationBody: "Cannot start HR Observation! Please enable Bluetooth and connect devices.",
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
