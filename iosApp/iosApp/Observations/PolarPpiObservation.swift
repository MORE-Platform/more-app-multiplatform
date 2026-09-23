//
//  PolarPpiObservation.swift
//  iosApp
//

import Combine
import CoreBluetooth
import Foundation
import KMPNativeCoroutinesCombine
import PolarBleSdk
import shared

class PolarPpiObservation: Observation_ {

    /// Substring-matched against the advertised name ("Polar 360 12345678"), so a Verity Sense or
    /// H10 cannot satisfy a Polar 360 observation.
    private let deviceIdentificer: Set<String> = [PolarController.polar360Model]
    private let bleManager = BluetoothStateManagement.shared
    private let controller = PolarController.shared

    private var streamTask: Task<Void, Never>?
    private var offlineRecordingTask: Task<Void, Never>?
    private var deviceListener: AnyCancellable?
    private var offlineMode = false
    private var stopBackgroundSync: BackgroundSync?

    private static let notificationBackoffInterval: TimeInterval = 60
    private static var lastCannotStartNotificationDate: Date?

    private static let dataKey = "polar360ppidata"
    private static let dataType: PolarDeviceDataType = .ppi

    /// Bounds of the offline recording, in Polar nanoseconds. Written by PolarController as it
    /// stops and lists the recording, read here to pad the sparse samples to 1 Hz.
    static var recording_startTimestamp: UInt64? = nil
    static var recording_endTimestamp: UInt64? = nil

    init(repos: MainRepository, sensorPermissions: Set<String>) {
        super.init(repos: repos, observationType: PolarPpiType(sensorPermissions: sensorPermissions))
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
                        Napier.e("PolarPpiObservation: Failed to fetch pending offline data: \(error)")
                        guard let self else { return }
                        self.offlineRecordingTask = self.controller.startOfflineRecording(
                            deviceId: deviceId, dataType: Self.dataType
                        )
                    }
                )
            } else {
                self.startStreaming(deviceId: deviceId)
            }
        }, onError: { error in Napier.e("Polar PPI setup error: \(error)") })
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
                finishBg = sync.begin(taskName: "PolarPpiStopAndFetch")
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
                    Napier.e("PolarPpiObservation: Failed to fetch offline data: \(error)")
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

    /// PPI padding is global (fills 1 Hz slots across the whole recording). Pad and persist in
    /// bounded chunks as we go, so the dense padded recording never becomes one giant in-memory
    /// list or row (OOM-safe for arbitrarily long recordings).
    private func storeRecording(_ items: [Any]) {
        let samples = items.compactMap { $0 as? PolarController.ppi_data }
        padAndStoreOneHz(
            samples: samples,
            startNs: PolarPpiObservation.recording_startTimestamp ?? 0,
            endNs: PolarPpiObservation.recording_endTimestamp ?? 0,
            dataKey: Self.dataKey
        )
    }

    /// Pads the sparse offline PPI recording to 1 Hz and persists it in bounded chunks *as it is
    /// produced*, so the whole dense recording (one row per second, gaps included) is never
    /// materialised at once. Peak added heap = the sorted raw samples (already resident) + one
    /// chunk — flat regardless of recording length.
    func padAndStoreOneHz(
        samples: [PolarController.ppi_data],
        startNs: UInt64,
        endNs: UInt64,
        dataKey: String,
        chunkSize: Int = PolarController.offlineStoreChunkSize
    ) {
        let step: UInt64 = 1_000_000_000
        var buffer: [PolarController.ppi_data] = []
        buffer.reserveCapacity(chunkSize)
        var total = 0
        var corrupted = 0

        func flush() {
            guard !buffer.isEmpty else { return }
            let processed = buffer.map {
                ["hr": $0.hr, "ppiInMs": $0.ppiInMs, "ppiErrorEstimate": $0.ppiErrorEstimate, "timestamp": $0.timestamp, "skinContact": $0.skinContact] as [String: Any]
            }
            storeData(data: [dataKey: processed], timestamp: -1) {}
            buffer.removeAll(keepingCapacity: true)
        }
        func emit(_ s: PolarController.ppi_data) {
            total += 1
            if s.ppiErrorEstimate == .max { corrupted += 1 }
            buffer.append(s)
            if buffer.count >= chunkSize { flush() }
        }

        // Guard cases where no padding is possible: store whatever raw samples exist, unchanged.
        guard endNs > 0 else { samples.forEach(emit); flush(); return }
        let sorted = samples.sorted { $0.timestamp < $1.timestamp }
        let rawStart: UInt64
        if let first = sorted.first {
            rawStart = startNs > 0 ? min(startNs, first.timestamp) : first.timestamp
        } else if startNs > 0 {
            rawStart = startNs   // no real samples: fill the whole window with corrupted slots
        } else {
            samples.forEach(emit); flush(); return
        }
        let effectiveStart = (rawStart / step) * step
        guard effectiveStart < endNs else { samples.forEach(emit); flush(); return }

        var sampleIndex = 0
        var cursor = effectiveStart
        while cursor < endNs {
            let slotEnd = cursor + step
            while sampleIndex < sorted.count && sorted[sampleIndex].timestamp < cursor {
                sampleIndex += 1
            }
            if sampleIndex < sorted.count && sorted[sampleIndex].timestamp < slotEnd {
                let s = sorted[sampleIndex]
                emit(PolarController.ppi_data(
                    hr: s.hr,
                    timestamp: cursor,
                    ppiInMs: s.ppiInMs,
                    ppiErrorEstimate: s.ppiErrorEstimate,
                    skinContact: s.skinContact ? 1 : 0
                ))
                sampleIndex += 1
                while sampleIndex < sorted.count && sorted[sampleIndex].timestamp < slotEnd {
                    sampleIndex += 1
                }
            } else {
                emit(PolarController.ppi_data(
                    hr: -99,
                    timestamp: cursor,
                    ppiInMs: 0,
                    ppiErrorEstimate: .max,
                    skinContact: 0
                ))
            }
            cursor += step
        }
        flush()
        Napier.d("PolarPpiObservation: padAndStoreOneHz — total=\(total), valid=\(total - corrupted), corrupted=\(corrupted)")
    }

    private func startStreaming(deviceId: String) {
        streamTask = Task { [weak self] in
            guard let self else { return }
            let api = self.controller.getPolarApi()
            do {
                for try await data in api.startPpiStreaming(deviceId) {
                    // Live samples are stored one row per sample, flat -- the offline dataKey
                    // wrapper is only for batched recordings.
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
                }
            } catch is CancellationError {
                // Expected: stop() cancelled the task.
            } catch {
                Napier.e("Polar PPI stream failed: \(error)")
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
    }

    override func shouldAutoPause() -> Bool { !offlineMode }

    override func bleDevicesNeeded() -> Set<String> { deviceIdentificer }

    override func ableToAutomaticallyStart() -> Bool { observerAccessible() }

    private func showCannotStartNotification() {
        let now = Date()
        if let last = PolarPpiObservation.lastCannotStartNotificationDate,
           now.timeIntervalSince(last) < PolarPpiObservation.notificationBackoffInterval {
            return
        }
        PolarPpiObservation.lastCannotStartNotificationDate = now
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
