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
    private var stopBackgroundSync: BackgroundSync?

    private static let notificationBackoffInterval: TimeInterval = 60
    private static var lastCannotStartNotificationDate: Date?
    
    static var recording_startTimestamp: UInt64? = nil
    static var recroding_endTimestamp: UInt64? = nil
    
    init(repos: MainRepository, sensorPermissions: Set<String>) {
        super.init(repos: repos, observationType: Polar360PpiType(sensorPermissions: sensorPermissions))
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

        controller.ensureReady(deviceId: deviceId, offlineMode: offlineMode, onReady: { [weak self] in
                guard let self else { return }
                if self.offlineMode {
                    self.controller.stopOfflineRecordingAndFetch(
                        dataType: .ppi,
                        onSuccess: { [weak self] items in
                            guard let self else { return }
                            let samples = items.compactMap { $0 as? Polar360Controller.ppi_data }
                            if !samples.isEmpty {
                                let padded = Polar360PpiObservation.padToOneHz(
                                    samples: samples,
                                    startNs: Polar360PpiObservation.recording_startTimestamp ?? 0,
                                    endNs: Polar360PpiObservation.recroding_endTimestamp ?? 0
                                )
                                let processed = padded.map { ["hr": $0.hr, "ppiInMs": $0.ppiInMs, "ppiErrorEstimate": $0.ppiErrorEstimate, "timestamp": $0.timestamp, "skinContact": $0.skinContact] as [String: Any] }
                                self.storeData(data: ["polar360ppidata": processed], timestamp: -1) {}
                            }
                            self.offlineRecordingDisposable = self.controller.startOfflineRecording(
                                deviceId: deviceId, dataType: .ppi
                            )
                        },
                        onError: { [weak self] error in
                            Napier.e("Polar360PpiObservation: Failed to fetch pending offline data: \(error)")
                            guard let self else { return }
                            self.offlineRecordingDisposable = self.controller.startOfflineRecording(
                                deviceId: deviceId, dataType: .ppi
                            )
                        }
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
                                            "ppiInMs": sample.ppInMs,
                                            "ppiErrorEstimate": sample.ppErrorEstimate,
                                            "timestamp": sample.timeStamp,
                                            "skinContact": sample.skinContactStatus == 1
                                        ],
                                        timestamp: -1
                                    ) { }
                                }
                            },
                            onError: { error in
                                Napier.e("Polar360 PPI stream failed: \(error)")
                            }
                        )
                }
            
        }, onError: { error in Napier.e("Polar360 PPI setup error: \(error)") })
        return true
    }

    override func stop(onCompletion: @escaping () -> Void) {
        deviceListener?.cancel()
        deviceListener = nil
        if offlineMode {
            offlineRecordingDisposable?.dispose()
            offlineRecordingDisposable = nil

            let finishBg: () -> Void
            if controller.appIsInBackground {
                let sync = BackgroundSync()
                stopBackgroundSync = sync
                finishBg = sync.begin(taskName: "Polar360PpiStopAndFetch")
            } else {
                finishBg = { [weak self] in self?.stopBackgroundSync = nil }
            }

            controller.stopOfflineRecordingAndFetch(
                dataType: .ppi,
                onSuccess: { [weak self] items in
                    guard let self else { onCompletion(); finishBg(); return }
                    let samples = items.compactMap { $0 as? Polar360Controller.ppi_data }
                    guard !samples.isEmpty else { onCompletion(); finishBg(); return }
                    let padded = Polar360PpiObservation.padToOneHz(
                        samples: samples,
                        startNs: Polar360PpiObservation.recording_startTimestamp ?? 0,
                        endNs: Polar360PpiObservation.recroding_endTimestamp ?? 0
                    )
                    let processed = padded.map { ["hr": $0.hr, "ppiInMs": $0.ppiInMs, "ppiErrorEstimate": $0.ppiErrorEstimate, "timestamp": $0.timestamp, "skinContact": $0.skinContact] as [String: Any] }
                    self.storeData(data: ["polar360ppidata": processed], timestamp: -1) {
                        onCompletion()
                        finishBg()
                    }
                },
                onError: { error in
                    Napier.e("Polar360PpiObservation: Failed to fetch offline data: \(error)")
                    onCompletion()
                    finishBg()
                }
            )
        } else {
            ppiDisposable?.dispose()
            ppiDisposable = nil
            onCompletion()
        }
    }

    /// Pads PPI samples to 1 Hz across [effectiveStart, endNs].
    /// Both startNs, endNs, and sample timestamps are in nanoseconds since 2000-01-01.
    /// Each 1-second slot gets the first sample whose timestamp falls in [cursor, cursor+1s),
    /// or a dummy (hr=-99, ppiErrorEstimate=UInt16.max) if no sample falls in that slot.
    static func padToOneHz(
        samples: [Polar360Controller.ppi_data],
        startNs: UInt64,
        endNs: UInt64
    ) -> [Polar360Controller.ppi_data] {
        guard endNs > 0, !samples.isEmpty else { return samples }

        let step: UInt64 = 1_000_000_000
        let sorted = samples.sorted { $0.timestamp < $1.timestamp }

        // Window start: take the earlier of recording_startTimestamp and first sample,
        // floored to a whole second, so the full recording period is covered.
        let firstSampleTs = sorted.first!.timestamp
        let rawStart = startNs > 0 ? min(startNs, firstSampleTs) : firstSampleTs
        let effectiveStart = (rawStart / step) * step

        guard effectiveStart < endNs else { return samples }

        // Safety cap: never generate more than 24 h of slots.
        let maxDurationNs: UInt64 = 24 * 3600 * 1_000_000_000
        guard endNs - effectiveStart <= maxDurationNs else {
            Napier.w("Polar360PpiObservation: padToOneHz — derived duration exceeds 24 h cap; returning raw samples")
            return samples
        }

        var result: [Polar360Controller.ppi_data] = []
        var sampleIndex = 0
        var cursor = effectiveStart

        while cursor < endNs {
            let slotEnd = cursor + step
            // Advance past any samples that fall before this slot.
            while sampleIndex < sorted.count && sorted[sampleIndex].timestamp < cursor {
                sampleIndex += 1
            }
            if sampleIndex < sorted.count && sorted[sampleIndex].timestamp < slotEnd {
                let s = sorted[sampleIndex]
                result.append(Polar360Controller.ppi_data(
                    hr: s.hr,
                    timestamp: cursor,
                    ppiInMs: s.ppiInMs,
                    ppiErrorEstimate: s.ppiErrorEstimate,
                    skinContact: s.skinContact ? 1 : 0
                ))
                sampleIndex += 1
                // Skip any additional samples within the same 1-second slot.
                while sampleIndex < sorted.count && sorted[sampleIndex].timestamp < slotEnd {
                    sampleIndex += 1
                }
            } else {
                result.append(Polar360Controller.ppi_data(
                    hr: -99,
                    timestamp: cursor,
                    ppiInMs: 0,
                    ppiErrorEstimate: .max,
                    skinContact: 0
                ))
            }
            cursor += step
        }

        let validCount = result.filter { $0.ppiErrorEstimate != .max }.count
        Napier.d("Polar360PpiObservation: padToOneHz — total=\(result.count), valid=\(validCount), corrupted=\(result.count - validCount)")
        return result
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

    override func ableToAutomaticallyStart() -> Bool { observerAccessible() }

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
