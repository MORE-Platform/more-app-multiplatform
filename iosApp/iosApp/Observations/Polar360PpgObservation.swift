//
//  Polar360PpiObservation.swift
//  iosApp
//

import Combine
import CoreBluetooth
import Foundation
import KMPNativeCoroutinesCombine
import PolarBleSdk
import RxSwift
import shared

class Polar360PpiObservation: Observation_ {

    private let deviceIdentificer: Set<String> = ["Polar", "360"]
    private let bleManager = BluetoothStateManagement.shared
    private let controller = Polar360Controller.shared

    private var ppiDisposable: Disposable?
    private var offlineRecordingDisposable: Disposable?
    private var deviceListener: AnyCancellable?
    private var offlineMode = false

    private static let notificationBackoffInterval: TimeInterval = 60
    private static var lastCannotStartNotificationDate: Date?

    init(repos: MainRepository, sensorPermissions: Set<String>) {
        super.init(repos: repos, observationType: Polar360PpiType(sensorPermissions: sensorPermissions))
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
                        deviceId: deviceId, dataType: .ppi
                    )
                } else {
                    self.ppiDisposable = self.controller.getPolarApi()
                        .startPpiStreaming(deviceId)
                        .subscribe(on: MainScheduler.instance)
                        .observe(on: ConcurrentDispatchQueueScheduler(qos: .background))
                        .subscribe(
                            onNext: { [weak self] data in
                                guard let self else { return }
                                for sample in data.samples {
                                    self.storeData(
                                        data: [
                                            "hr": sample.hr,
                                            "ppiInMs": sample.ppi,
                                            "ppiErrorEstimate": sample.errorEstimate,
                                            "timestamp": sample.timeStamp
                                        ],
                                        timestamp: -1
                                    ) { }
                                }
                            },
                            onError: { error in
                                print("Polar360 PPI stream failed: \(error)")
                            }
                        )
                }
            },
            onError: { error in print("Polar360 PPI setup error: \(error)") }
        )
        return true
    }

    override func stop(onCompletion: @escaping () -> Void) {
        if offlineMode {
            controller.stopOfflineRecording(dataType: .ppi)
            offlineRecordingDisposable?.dispose()
            offlineRecordingDisposable = nil
        } else {
            ppiDisposable?.dispose()
            ppiDisposable = nil
        }
        deviceListener?.cancel()
        deviceListener = nil
        onCompletion()
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
        if let last = Polar360PpiObservation.lastCannotStartNotificationDate,
           now.timeIntervalSince(last) < Polar360PpiObservation.notificationBackoffInterval {
            return
        }
        Polar360PpiObservation.lastCannotStartNotificationDate = now
        showObservationErrorNotification(
            notificationBody: "Cannot start PPI Observation! Please enable Bluetooth and connect devices.",
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
