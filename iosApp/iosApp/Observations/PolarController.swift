//
//  PolarController.swift
//  iosApp
//
//  Drives Polar offline recording and device setup for every Polar observation
//  (ACC, HR, PPI, TEMP).
//
//  Polar SDK 8 replaced the RxSwift surface with async/await: every call here is
//  `async throws` or iterates an `AsyncThrowingStream`. The pre-SDK8 implementation
//  serialised BLE work through a `PublishSubject` + `concatMap` operation queue;
//  `bleLock` is the direct async equivalent, and mirrors the `bleMutex` in the
//  Android PolarController.
//

import Foundation
import PolarBleSdk
import shared

enum PolarError: LocalizedError {
    case corruptedRecording(String)

    var errorDescription: String? {
        switch self {
        case .corruptedRecording(let msg):
            return "Polar: corrupted recording — \(msg)"
        }
    }
}

/// Mutual exclusion that actually holds across `await`.
///
/// A bare `actor` is not enough: actors are reentrant, so a multi-step BLE sequence
/// would interleave with another observation's at every suspension point. Waiters
/// queue here in FIFO order instead, so a stop/drain/start cycle runs to completion
/// before the next one begins.
actor BleLock {
    private var isLocked = false
    private var waiters: [CheckedContinuation<Void, Never>] = []

    func acquire() async {
        if !isLocked {
            isLocked = true
            return
        }
        await withCheckedContinuation { waiters.append($0) }
    }

    func release() {
        if waiters.isEmpty {
            isLocked = false
        } else {
            waiters.removeFirst().resume()
        }
    }
}

final class PolarController {
    static let shared = PolarController()

    static let CONFIG_OFFLINE_RECORDING = "Offline_recording"

    /// Max samples stored per observation_data row when persisting an extracted offline
    /// recording. A whole recording can hold hundreds of thousands of samples; serialising
    /// them into one row (and later into one upload body) OOM-kills the app. Store in
    /// bounded chunks instead. Mirrors the Android OFFLINE_STORE_CHUNK_SIZE.
    static let offlineStoreChunkSize = 1000

    /// Substring matched against the advertised device name ("Polar 360 12345678").
    static let polar360Model = "360"

    private let polarConnector = AppDelegate.polarConnector
    private let bleManager = BluetoothStateManagement.shared

    private var sdkModeEnabled = false
    private var currentDeviceId: String?
    private var activeRecordingTypes: Set<PolarDeviceDataType> = []

    /// Updated by iOSApp on every scene-phase transition so observations can branch between
    /// foreground and background stop behaviour without importing UIKit or touching
    /// UIApplication on an unknown thread.
    var appIsInBackground: Bool = false

    private let bleLock = BleLock()

    private init() {}

    // MARK: - Sample models

    class ppi_data: Codable {
        let hr: Int
        let timestamp: UInt64
        let ppiInMs: UInt16
        let ppiErrorEstimate: UInt16
        let skinContact: Bool
        init(hr: Int, timestamp: UInt64, ppiInMs: UInt16, ppiErrorEstimate: UInt16, skinContact: Int) {
            self.hr = hr
            self.timestamp = timestamp
            self.ppiInMs = ppiInMs
            self.ppiErrorEstimate = ppiErrorEstimate
            self.skinContact = skinContact == 1
        }
    }

    class acc_data: Codable {
        let x: Int32
        let y: Int32
        let z: Int32
        let timestamp: UInt64

        init(x: Int32, y: Int32, z: Int32, timestamp: UInt64) {
            self.x = x
            self.y = y
            self.z = z
            self.timestamp = timestamp
        }
    }

    class temp_data: Codable {
        let temp: Float
        let timestamp: UInt64

        init(temp: Float, Timestamp: UInt64) {
            self.temp = temp
            self.timestamp = Timestamp
        }
    }

    class hr_data: Codable {
        let hr: Int
        let timestamp: UInt64
        let skinContact: Bool
        init(hr: Int, timestamp: UInt64, skinContact: Bool) {
            self.hr = hr
            self.timestamp = timestamp
            self.skinContact = skinContact
        }
    }

    // MARK: - Device lookup

    func findPolarDevice(model: String = PolarController.polar360Model) -> BluetoothDeviceEntity? {
        return bleManager.connectedDevicesValue.first { device in
            guard let name = device.deviceName?.lowercased() else { return false }
            return name.contains("polar") && name.contains(model) && device.address != nil
        }
    }

    func getPolarApi() -> PolarBleApi {
        return polarConnector.polarApi
    }

    func isOfflineRecordingMode(config: [String: Any]) -> Bool {
        if let offlineRecording = config[PolarController.CONFIG_OFFLINE_RECORDING] {
            return String(describing: offlineRecording) == "true"
        }
        return false
    }

    // MARK: - Setup

    /// Runs first-time-use setup (if the device still needs it), syncs the device clock and
    /// clears SDK mode, then reports back via `onReady` / `onError`.
    ///
    /// Callback-shaped rather than `async` because observations start from
    /// `Observation_.start() -> Bool`, which cannot await.
    func ensureReady(deviceId: String, offlineMode: Bool, onReady: @escaping () -> Void, onError: @escaping (any Swift.Error) -> Void) {
        currentDeviceId = deviceId
        saveDeviceIdForBackground()
        Task { [weak self] in
            guard let self else { return }
            await self.bleLock.acquire()
            do {
                try await self.checkIfDeviceIsSetupLocked(identifier: deviceId)
                await self.syncDeviceTimeLocked(identifier: deviceId)
                // Offline recording of PPI/HR requires SDK mode to be OFF, in both modes.
                await self.disableSdkModeIfNeededLocked(identifier: deviceId)
                await self.bleLock.release()
                onReady()
            } catch {
                await self.bleLock.release()
                Napier.e("PolarController: ensureReady failed: \(error)")
                onError(error)
            }
        }
    }

    // MARK: - Recording

    /// Starts an offline recording for `dataType`, clearing any recording still running for it
    /// first. Fire-and-forget: returns the `Task` doing the work so the caller can cancel it
    /// (the SDK 8 replacement for the RxSwift `Disposable`).
    @discardableResult
    func startOfflineRecording(deviceId: String, dataType: PolarDeviceDataType, settings: PolarSensorSetting? = nil) -> Task<Void, Never> {
        Napier.i("PolarController: [\(dataType)] Starting offline recording on device=\(deviceId)")
        activeRecordingTypes.insert(dataType)
        return Task { [weak self] in
            guard let self else { return }
            await self.bleLock.acquire()
            defer { Task { await self.bleLock.release() } }

            await self.logOfflineRecordingStateLocked(deviceId, tag: "\(dataType) PRE-START")
            await self.disableSdkModeIfNeededLocked(identifier: deviceId)

            // A recording still running keeps its file open; stop it before starting fresh.
            do {
                try await self.polarConnector.polarApi.stopOfflineRecording(deviceId, feature: dataType)
            } catch {
                Napier.w("PolarController: [\(dataType)] pre-stop error (ignored): \(error)")
            }

            do {
                try await self.resolveAndStartOfflineRecordingLocked(deviceId: deviceId, dataType: dataType, settings: settings)
                Napier.i("PolarController: [\(dataType)] Offline recording started successfully")
                await self.logOfflineRecordingStateLocked(deviceId, tag: "\(dataType) POST-START-OK")
            } catch {
                Napier.e("PolarController: [\(dataType)] Failed to start: \(error)")
                await self.logOfflineRecordingStateLocked(deviceId, tag: "\(dataType) POST-FAIL")
                return
            }

            // Starting one type can knock the others off on some firmware; put them back.
            let others = self.activeRecordingTypes.filter { $0 != dataType }
            guard !others.isEmpty else { return }
            Napier.i("PolarController: [\(dataType)] Restarting other active recordings: \(others)")
            for otherType in others {
                do {
                    try await self.resolveAndStartOfflineRecordingLocked(deviceId: deviceId, dataType: otherType, settings: nil)
                    Napier.i("PolarController: [\(otherType)] Restarted after [\(dataType)] start")
                } catch {
                    Napier.w("PolarController: [\(otherType)] Restart failed (ignored): \(error)")
                }
            }
        }
    }

    func stopOfflineRecording(dataType: PolarDeviceDataType) {
        guard let deviceId = currentDeviceId else { return }
        Task { [weak self] in
            guard let self else { return }
            await self.bleLock.acquire()
            defer { Task { await self.bleLock.release() } }
            do {
                try await self.polarConnector.polarApi.stopOfflineRecording(deviceId, feature: dataType)
                Napier.i("PolarController: Stopped offline recording for \(dataType)")
            } catch {
                Napier.e("PolarController: Could not stop offline recording for \(dataType): \(error)")
            }
        }
    }

    /// Stops the running recording for `dataType` and reads every recording of that type off the
    /// device, handing back the raw samples and deleting the entries.
    ///
    /// Callers persist the result in bounded windows via `storeOfflineChunked` — never log or copy
    /// it whole, a long recording holds hundreds of thousands of samples.
    func stopOfflineRecordingAndFetch(dataType: PolarDeviceDataType, onSuccess: @escaping ([Any]) -> Void, onError: @escaping (any Swift.Error) -> Void) {
        activeRecordingTypes.remove(dataType)
        // If the in-memory ID was cleared by a mid-sequence BLE disconnect while the app is in the
        // background, fall back to the value persisted in UserDefaults so the remaining queued
        // observations can still complete their fetch.
        if currentDeviceId == nil && appIsInBackground {
            restoreDeviceIdFromBackground()
        }
        guard let deviceId = currentDeviceId else {
            Napier.w("PolarController: [\(dataType)] currentDeviceId is nil — returning empty")
            onSuccess([])
            return
        }
        Napier.d("PolarController: [\(dataType)] Enqueueing stop+fetch for device=\(deviceId)")
        Task { [weak self] in
            guard let self else { return }
            await self.bleLock.acquire()
            do {
                let samples = try await self.stopAndFetchLocked(deviceId: deviceId, dataType: dataType)
                await self.bleLock.release()
                Napier.i("PolarController: [\(dataType)] Task completed with \(samples.count) items")
                onSuccess(samples)
            } catch {
                await self.bleLock.release()
                Napier.e("PolarController: [\(dataType)] stop+fetch failed: \(error)")
                onError(error)
            }
        }
    }

    // MARK: - Operations below assume bleLock is held

    private func stopAndFetchLocked(deviceId: String, dataType: PolarDeviceDataType) async throws -> [Any] {
        if dataType == .ppi {
            let epoch2000: TimeInterval = 946_684_800
            let endNs = UInt64(max(0, Date().timeIntervalSince1970 - epoch2000)) * 1_000_000_000
            PolarPpiObservation.recording_endTimestamp = endNs
            Napier.d("PolarController: [ppi] recording_endTimestamp=\(endNs)")
        }

        Napier.d("PolarController: [\(dataType)] Stopping recording on device=\(deviceId)")
        do {
            try await polarConnector.polarApi.stopOfflineRecording(deviceId, feature: dataType)
        } catch {
            Napier.w("PolarController: [\(dataType)] stopOfflineRecording API error (ignored): \(error)")
        }

        Napier.d("PolarController: [\(dataType)] Recording stopped, listing all recordings...")
        var entries: [PolarOfflineRecordingEntry] = []
        do {
            for try await entry in polarConnector.polarApi.listOfflineRecordings(deviceId) {
                if entry.type == dataType {
                    entries.append(entry)
                }
            }
        } catch {
            Napier.e("PolarController: [\(dataType)] listOfflineRecordings error: \(error)")
            throw error
        }
        // Only the count is logged — logging one line per entry floods the logger and can kill the
        // app when a device holds hundreds of recordings.
        Napier.d("PolarController: [\(dataType)] Listed \(entries.count) matching entries")

        var all: [Any] = []
        var emptyEntries = 0
        for entry in entries {
            if dataType == .ppi {
                let epoch2000: TimeInterval = 946_684_800
                let startSecs = entry.date.timeIntervalSince1970 - epoch2000
                let startNs = startSecs > 0 ? UInt64(startSecs) * 1_000_000_000 : 0
                PolarPpiObservation.recording_startTimestamp = startNs
            }

            let data: PolarOfflineRecordingData
            do {
                data = try await polarConnector.polarApi.getOfflineRecord(deviceId, entry: entry, secret: nil)
            } catch {
                // Unreadable/corrupt entry: remove it so it does not accumulate on the device
                // forever, then move on with no samples from it.
                Napier.e("PolarController: [\(dataType)] getOfflineRecord error: \(error) — removing unreadable entry")
                await removeQuietly(deviceId: deviceId, entry: entry)
                continue
            }

            // NOTE: never log per entry here — neither the sample list (a long recording
            // stringifies hundreds of thousands of samples into one String) nor one line per
            // entry, which floods the logger when many recordings are read in one go.
            if case .emptyData = data { emptyEntries += 1 }
            let samples = extractSamples(from: data)
            await removeQuietly(deviceId: deviceId, entry: entry)
            all.append(contentsOf: samples)
        }
        if emptyEntries > 0 {
            // Aggregated on purpose: one warning per empty recording floods the logger.
            Napier.w("PolarController: [\(dataType)] \(emptyEntries) of \(entries.count) entries were empty — likely no skin contact during those recordings")
        }
        Napier.i("PolarController: [\(dataType)] Total samples returned: \(all.count)")
        return all
    }

    /// Removes an entry, never propagating a removal failure: samples already read must not be
    /// lost just because the device refused the delete.
    private func removeQuietly(deviceId: String, entry: PolarOfflineRecordingEntry) async {
        do {
            try await polarConnector.polarApi.removeOfflineRecord(deviceId, entry: entry)
        } catch {
            Napier.e("PolarController: removeOfflineRecord error (ignored): \(error)")
        }
    }

    private func resolveAndStartOfflineRecordingLocked(deviceId: String, dataType: PolarDeviceDataType, settings: PolarSensorSetting?) async throws {
        // PPI and HR do not accept settings in normal offline mode.
        if dataType == .ppi || dataType == .hr {
            Napier.d("PolarController: [\(dataType)] Starting offline recording with nil settings")
            try await polarConnector.polarApi.startOfflineRecording(deviceId, feature: dataType, settings: nil, secret: nil)
            return
        }
        do {
            let chosenSettings: PolarSensorSetting
            if let settings {
                chosenSettings = settings
            } else {
                // ACC and temperature: prefer the device's requested settings (known-good on
                // current firmware).
                let resolved = try await polarConnector.polarApi.requestOfflineRecordingSettings(deviceId, feature: dataType)
                // Passing the queried settings as-is lets the SDK resolve to the device's max
                // sample rate (4 Hz for skin temperature). Temperature must be recorded at 1 Hz,
                // so pin sampleRate to 1 while keeping the device's native resolution/channels.
                chosenSettings = isTemperature(dataType) ? pinTemperatureSampleRate(resolved) : resolved
            }
            Napier.d("PolarController: [\(dataType)] Starting offline recording with settings: \(chosenSettings)")
            try await polarConnector.polarApi.startOfflineRecording(deviceId, feature: dataType, settings: chosenSettings, secret: nil)
        } catch {
            // Some newer firmware fails settings negotiation (notably for temperature) — fall back
            // to a settings-less start.
            Napier.w("PolarController: [\(dataType)] Settings-based start failed (\(error)); retrying without settings")
            try await polarConnector.polarApi.startOfflineRecording(deviceId, feature: dataType, settings: nil, secret: nil)
        }
    }

    private func isTemperature(_ dataType: PolarDeviceDataType) -> Bool {
        return dataType == .temperature || dataType == .skinTemperature
    }

    /// Builds a concrete setting from the device's available offline settings, forcing the sample
    /// rate to 1 Hz (the documented native rate for skin temperature). Every other setting type
    /// keeps the device's max available value so resolution/channels stay valid. Falls back to the
    /// unmodified settings if the concrete initializer rejects the combination.
    private func pinTemperatureSampleRate(_ available: PolarSensorSetting) -> PolarSensorSetting {
        var concrete: [PolarSensorSetting.SettingType: UInt32] = [:]
        for (type, values) in available.settings {
            if type == .sampleRate {
                concrete[type] = values.contains(1) ? 1 : (values.min() ?? 1)
            } else {
                concrete[type] = values.max() ?? 0
            }
        }
        return (try? PolarSensorSetting(concrete)) ?? available
    }

    /// Diagnostic: logs which offline-recording data types the device SUPPORTS and which are
    /// CURRENTLY recording. Used to explain "Invalid state" start failures (e.g. a skin-temp
    /// recording already stuck active on the device that stop didn't clear).
    private func logOfflineRecordingStateLocked(_ deviceId: String, tag: String) async {
        do {
            let types = try await polarConnector.polarApi.getAvailableOfflineRecordingDataTypes(deviceId)
            Napier.d("PolarController: [\(tag)] device SUPPORTS offline types = \(types.map { "\($0)" }.sorted())")
        } catch {
            Napier.w("PolarController: [\(tag)] getAvailableOfflineRecordingDataTypes failed: \(error)")
        }
        do {
            let status = try await polarConnector.polarApi.getOfflineRecordingStatus(deviceId)
            let recording = status.filter { $0.value }.keys.map { "\($0)" }.sorted()
            Napier.d("PolarController: [\(tag)] currently RECORDING = \(recording.isEmpty ? "none" : recording.joined(separator: ", ")) | full=\(status)")
        } catch {
            Napier.w("PolarController: [\(tag)] getOfflineRecordingStatus failed: \(error)")
        }
    }

    private func disableSdkModeIfNeededLocked(identifier: String) async {
        guard sdkModeEnabled else { return }
        do {
            try await polarConnector.polarApi.disableSDKMode(identifier)
            sdkModeEnabled = false
            Napier.d("PolarController: SDK mode disabled for \(identifier)")
        } catch {
            Napier.w("PolarController: disableSDKMode error (ignored): \(error)")
        }
    }

    private func syncDeviceTimeLocked(identifier: String) async {
        do {
            try await polarConnector.polarApi.setLocalTime(identifier, time: Date(), zone: TimeZone.current)
            Napier.i("PolarController: Device time synced for \(identifier)")
        } catch {
            Napier.w("PolarController: Failed to sync device time (ignored): \(error)")
        }
    }

    private func checkIfDeviceIsSetupLocked(identifier: String) async throws {
        if try await polarConnector.polarApi.isFtuDone(identifier) {
            Napier.i("PolarController: FTU already done")
            return
        }

        let dateFormatter = ISO8601DateFormatter()
        dateFormatter.formatOptions = [.withInternetDateTime]
        dateFormatter.timeZone = TimeZone.current

        let profile = PolarUserProfile.load()
        let birthDate = profile?.birthDate ?? Calendar.current.date(byAdding: .year, value: -30, to: Date()) ?? Date()
        let maxHR = max(120, min(220, 220 - (profile?.age ?? 30)))

        let ftuConfig = PolarFirstTimeUseConfig(
            gender: profile?.gender.polarGender ?? .female,
            birthDate: birthDate,
            height: Float(profile?.heightCm ?? 170),
            weight: Float(profile?.weightKg ?? 70),
            maxHeartRate: Int(maxHR),
            vo2Max: 80,
            restingHeartRate: 60,
            trainingBackground: PolarFirstTimeUseConfig.TrainingBackground.frequent,
            deviceTime: dateFormatter.string(from: Date()),
            typicalDay: PolarFirstTimeUseConfig.TypicalDay.mostlyMoving,
            sleepGoalMinutes: 480
        )

        try await polarConnector.polarApi.doFirstTimeUse(identifier, ftuConfig: ftuConfig)
    }

    // MARK: - Sample extraction

    private func extractSamples(from data: PolarOfflineRecordingData) -> [Any] {
        switch data {
        case let .accOfflineRecordingData(accData, _, _):
            return accData.map {
                acc_data(x: $0.x, y: $0.y, z: $0.z, timestamp: $0.timeStamp)
            }

        case let .ppiOfflineRecordingData(ppiData, _):
            return ppiData.samples.map {
                ppi_data(hr: $0.hr, timestamp: $0.timeStamp,
                         ppiInMs: $0.ppInMs, ppiErrorEstimate: $0.ppErrorEstimate, skinContact: $0.skinContactStatus)
            }

        case let .temperatureOfflineRecordingData(tempData, _):
            return tempData.samples.map {
                temp_data(temp: $0.temperature, Timestamp: $0.timeStamp)
            }

        case let .skinTemperatureOfflineRecordingData(tempData, _):
            return tempData.samples.map {
                temp_data(temp: $0.temperature, Timestamp: $0.timeStamp)
            }

        case .emptyData:
            // Reported in aggregate by the caller — never log once per entry.
            return []

        default:
            Napier.w("PolarController: extractSamples — unhandled PolarOfflineRecordingData case")
            return []
        }
    }

    // MARK: - Lifecycle / background device id

    func onDeviceDisconnected() {
        sdkModeEnabled = false
        currentDeviceId = nil
        // Only clear the persisted ID in the foreground — a background disconnect (e.g. device
        // temporarily out of range) must not erase the ID the BGAppRefreshTask needs to reconnect.
        if !appIsInBackground {
            clearBackgroundDeviceId()
        }
    }

    // Key deliberately keeps the original "polar360." prefix so a background task launched by an
    // existing install still finds the device id it saved before the rename.
    private static let backgroundDeviceIdKey = "polar360.backgroundDeviceId"

    /// Persists the current device ID so it survives a cold background launch when the
    /// BGAppRefreshTask fires.
    func saveDeviceIdForBackground() {
        AppDelegate.appGroupUserDefaults?.set(currentDeviceId, forKey: Self.backgroundDeviceIdKey)
        Napier.i("PolarController: saved deviceId '\(currentDeviceId ?? "nil")' for background")
    }

    /// Restores the previously saved device ID — call at the start of the BGAppRefreshTask handler
    /// so stopOfflineRecordingAndFetch can find the device.
    func restoreDeviceIdFromBackground() {
        currentDeviceId = AppDelegate.appGroupUserDefaults?.string(forKey: Self.backgroundDeviceIdKey)
        Napier.i("PolarController: restored deviceId '\(currentDeviceId ?? "nil")' from background")
    }

    func clearBackgroundDeviceId() {
        AppDelegate.appGroupUserDefaults?.removeObject(forKey: Self.backgroundDeviceIdKey)
        Napier.i("PolarController: cleared background deviceId")
    }

    // MARK: - Offline mode persistence

    private static func offlineModeKey(_ dataKey: String) -> String { "polar360.offlineMode.\(dataKey)" }

    /// Remembers whether the observation behind `dataKey` runs as an offline recording.
    ///
    /// Offline mode is part of the observation's configuration, but the config is only applied when
    /// an observation is started. A Polar observation that has never been started in this process —
    /// after a cold launch, or when the last start failed because the device was out of range —
    /// would therefore report `offlineMode == false`, get auto-paused by the periodic task-state
    /// update while the device is away, and send the UI back to the "start data capture" screen even
    /// though the device is still recording. Persisting the flag keeps that knowledge across
    /// instances and launches.
    func persistOfflineMode(_ enabled: Bool, for dataKey: String) {
        AppDelegate.appGroupUserDefaults?.set(enabled, forKey: Self.offlineModeKey(dataKey))
    }

    /// The last persisted offline mode for `dataKey` — false when the observation has never run.
    func restoredOfflineMode(for dataKey: String) -> Bool {
        AppDelegate.appGroupUserDefaults?.bool(forKey: Self.offlineModeKey(dataKey)) ?? false
    }
}

// Heap-safe persistence for large Polar offline recordings. The SDK returns the whole recording at
// once; storing it as one observation_data row builds a giant JSON blob that OOM-kills the app on
// serialise/upload. Windowing by index and calling storeData once per bounded chunk keeps every DB
// row — and every later upload sub-bulk — small. Mirrors the Android observations' windowed store.
extension Observation_ {
    func storeOfflineChunked<T>(
        _ samples: [T],
        dataKey: String,
        chunkSize: Int = PolarController.offlineStoreChunkSize,
        transform: ([T]) -> [[String: Any]]
    ) {
        guard !samples.isEmpty else { return }
        var index = 0
        while index < samples.count {
            let end = Swift.min(index + chunkSize, samples.count)
            let processed = transform(Array(samples[index..<end]))
            if !processed.isEmpty {
                storeData(data: [dataKey: processed], timestamp: -1) {}
            }
            index = end
        }
    }
}
