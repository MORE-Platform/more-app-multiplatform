//
//  PolarAccObservation.swift
//  iosApp
//

import Combine
import CoreBluetooth
import Foundation
import KMPNativeCoroutinesCombine
import PolarBleSdk
import shared

class PolarAccObservation: Observation_ {

    /// Substring-matched against the advertised name ("Polar 360 12345678"), so a Verity Sense or
    /// H10 cannot satisfy a Polar 360 observation.
    private let deviceIdentificer: Set<String> = [PolarController.polar360Model]
    private let bleManager = BluetoothStateManagement.shared
    private let controller = PolarController.shared

    private var streamTask: Task<Void, Never>?
    private var offlineRecordingTask: Task<Void, Never>?
    private var deviceListener: AnyCancellable?
    /// Restored from the last applied config so an instance that has not been started yet
    /// (cold launch, or a start that failed while the device was out of range) already knows it
    /// is an offline recording and must not be auto-paused. See
    /// `PolarController.persistOfflineMode(_:for:)`.
    private var offlineMode = PolarController.shared.restoredOfflineMode(for: PolarAccObservation.dataKey)
    private var stopBackgroundSync: BackgroundSync?

    private static let notificationBackoffInterval: TimeInterval = 60
    private static var lastCannotStartNotificationDate: Date?

    private static let dataKey = "polar360accdata"
    private static let dataType: PolarDeviceDataType = .acc

    init(repos: MainRepository, sensorPermissions: Set<String>) {
        super.init(repos: repos, observationType: PolarAccType(sensorPermissions: sensorPermissions))
    }

    override func start() -> Bool {
        if !observerAccessible() {
            showCannotStartNotification()
            return false
        }

        guard let device = controller.findPolarDevice() else {
            showCannotStartNotification()
            return false
        }
        let deviceId = device.deviceId

        listenToDeviceConnection()

        controller.ensureReady(deviceId: deviceId, offlineMode: offlineMode, onReady: { [weak self] in
            guard let self else { return }
            if self.offlineMode {
                self.controller.stopOfflineRecordingAndFetch(
                    dataType: Self.dataType,
                    onSuccess: { [weak self] items in
                        guard let self else { return }
                        self.storeRecording(items)
                        self.offlineRecordingTask = self.controller.startOfflineRecording(
                            deviceId: deviceId, dataType: Self.dataType
                        )
                    },
                    onError: { [weak self] error in
                        Napier.e("PolarAccObservation: Failed to fetch pending offline data: \(error)")
                        guard let self else { return }
                        self.offlineRecordingTask = self.controller.startOfflineRecording(
                            deviceId: deviceId, dataType: Self.dataType
                        )
                    }
                )
            } else {
                self.startStreaming(deviceId: deviceId)
            }
        }, onError: { error in Napier.e("Polar ACC setup error: \(error)") })
        return true
    }

    override func stop(onCompletion: @escaping () -> Void) {
        deviceListener?.cancel()
        deviceListener = nil
        if offlineMode {
            offlineRecordingTask?.cancel()
            offlineRecordingTask = nil

            let finishBg: () -> Void
            if controller.appIsInBackground {
                let sync = BackgroundSync()
                stopBackgroundSync = sync
                finishBg = sync.begin(taskName: "PolarAccStopAndFetch")
            } else {
                finishBg = { [weak self] in self?.stopBackgroundSync = nil }
            }

            controller.stopOfflineRecordingAndFetch(
                dataType: Self.dataType,
                onSuccess: { [weak self] items in
                    guard let self else { onCompletion(); finishBg(); return }
                    self.storeRecording(items)
                    onCompletion()
                    finishBg()
                },
                onError: { error in
                    Napier.e("PolarAccObservation: Failed to fetch offline data: \(error)")
                    onCompletion()
                    finishBg()
                }
            )
        } else {
            streamTask?.cancel()
            streamTask = nil
            onCompletion()
        }
    }

    /// Stores the whole recording in bounded chunks so a long recording never becomes one giant
    /// row that OOM-kills the app on serialise/upload.
    private func storeRecording(_ items: [Any]) {
        let samples = items.compactMap { $0 as? PolarController.acc_data }
        storeOfflineChunked(samples, dataKey: Self.dataKey) { window in
            window.map { ["x": $0.x, "y": $0.y, "z": $0.z, "timestamp": $0.timestamp] as [String: Any] }
        }
    }

    private func startStreaming(deviceId: String) {
        streamTask = Task { [weak self] in
            guard let self else { return }
            let api = self.controller.getPolarApi()
            let settings: PolarSensorSetting
            do {
                settings = try await api.requestStreamSettings(deviceId, feature: Self.dataType)
            } catch {
                Napier.e("Polar ACC settings request failed: \(error)")
                guard let fallback = try? PolarSensorSetting([.sampleRate: 52, .resolution: 1]) else { return }
                settings = fallback
            }
            do {
                for try await data in api.startAccStreaming(deviceId, settings: settings) {
                    guard let sample = data.first else { continue }
                    // Live samples are stored one row per sample, flat -- the offline dataKey
                    // wrapper is only for batched recordings.
                    self.storeData(
                        data: [
                            "x": sample.x,
                            "y": sample.y,
                            "z": sample.z,
                            "timestamp": sample.timeStamp
                        ],
                        timestamp: -1
                    ) { }
                }
            } catch is CancellationError {
                // Expected: stop() cancelled the task.
            } catch {
                Napier.e("Polar Accelerometer stream failed: \(error)")
            }
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
        controller.persistOfflineMode(offlineMode, for: Self.dataKey)
    }

    override func shouldAutoPause() -> Bool { !offlineMode }

    override func bleDevicesNeeded() -> Set<String> { deviceIdentificer }

    override func ableToAutomaticallyStart() -> Bool { observerAccessible() }

    private func showCannotStartNotification() {
        let now = Date()
        if let last = PolarAccObservation.lastCannotStartNotificationDate,
           now.timeIntervalSince(last) < PolarAccObservation.notificationBackoffInterval {
            return
        }
        PolarAccObservation.lastCannotStartNotificationDate = now
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
                guard let self else { return }
                if !self.deviceIdentificer.anyNameIn(items: devices) {
                    if self.offlineMode {
                        // In offline mode the device records independently — keep running
                    } else {
                        self.controller.onDeviceDisconnected()
                        self.deviceListener?.cancel()
                    }
                }
            })
    }
}
