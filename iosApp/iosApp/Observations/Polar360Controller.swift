//
//  Polar360Controller.swift
//  iosApp
//
//  Created on 2026-03-11.
//

import Foundation
import PolarBleSdk
import RxSwift
import shared

class Polar360Controller {
    static let shared = Polar360Controller()
    static let CONFIG_OFFLINE_RECORDING = "Offline_recording"

    private let polarConnector = AppDelegate.polarConnector
    private let bleManager = BluetoothStateManagement.shared

    private var sdkModeEnabled = false
    private var currentDeviceId: String?
    /// Updated by iOSApp on every scene-phase transition so observations
    /// can branch between foreground and background stop behaviour without
    /// importing UIKit or touching UIApplication on an unknown thread.
    var appIsInBackground: Bool = false
    private let disposeBag = DisposeBag()
    private let operationQueue = PublishSubject<Observable<[Any]>>()

    private init() {
        operationQueue
            .concatMap { $0 }
            .subscribe(
                onNext: { _ in },
                onError: { error in Napier.e("Polar360Controller: BLE operation queue error: \(error)") }
            )
            .disposed(by: disposeBag)
    }


    class ppi_data: Codable {
        let hr: Int
        let timestamp: UInt64
        let ppiInMs : UInt16
        let ppiErrorEstimate : UInt16
        let skinContact  : Bool
        init(hr:Int, timestamp:UInt64, ppiInMs:UInt16, ppiErrorEstimate:UInt16,skinContact:Int){
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

        init(x:Int32 , y:Int32 , z: Int32 , timestamp:UInt64){
            self.x = x
            self.y = y
            self.z = z
            self.timestamp = timestamp
        }
    }

    class temp_data: Codable {
        let temp: Float
        let timestamp: UInt64

        init(temp:Float , Timestamp:UInt64){
            self.temp = temp
            self.timestamp = Timestamp
        }
    }

    class hr_data: Codable {
        let hr: Int
        let timestamp: UInt64
        let skinContact : Bool
        init(hr:Int, timestamp:UInt64 , skinContact : Bool){
            self.hr = hr
            self.timestamp = timestamp
            self.skinContact = skinContact
        }
    }


    func findPolar360Device() -> BluetoothDeviceEntity? {
        return bleManager.connectedDevicesValue.first { device in
            guard let name = device.deviceName?.lowercased() else { return false }
            return name.contains("polar") && name.contains("360") && device.address != nil
        }
    }

    func ensureReady(deviceId: String, offlineMode: Bool, onReady: @escaping () -> Void, onError: @escaping (Error) -> Void) {
        currentDeviceId = deviceId
        saveDeviceIdForBackground()
        let task = checkIfDeviceIsSetup(identifier: deviceId)
            .andThen(offlineMode ? disableSdkModeIfNeeded(identifier: deviceId) : enableSdkModeIfNeeded(identifier: deviceId))
            .andThen(Single<[Any]>.just([]))
            .catch { error -> Single<[Any]> in
                Napier.e("Polar360Controller: ensureReady failed: \(error)")
                return .just([])
            }
            .do(onSuccess: { _ in onReady() })
            .asObservable()
        operationQueue.onNext(task)
    }

    func startOfflineRecording(deviceId: String, dataType: PolarDeviceDataType, settings: PolarSensorSetting? = nil) -> Disposable {
        Napier.i("Polar360Controller: [\(dataType)] Starting offline recording on device=\(deviceId)")
        let disableSdk = Completable.deferred {
            do {
                return try self.polarConnector.polarApi.disableSDKMode(deviceId)
            } catch {
                return Completable.empty()
            }
        }
        .catch { error -> Completable in
            Napier.w("Polar360Controller: [\(dataType)] disableSDKMode error (ignored): \(error)")
            return Completable.empty()
        }
        .do(onCompleted: {
            Napier.d("Polar360Controller: [\(dataType)] SDK mode disabled, stopping any existing recording...")
        })

        let preStop = polarConnector.polarApi.stopOfflineRecording(deviceId, feature: dataType)
            .catch { error -> Completable in
                Napier.w("Polar360Controller: [\(dataType)] pre-stop error (ignored): \(error)")
                return Completable.empty()
            }

        return disableSdk
            .andThen(preStop)
            .andThen(resolveAndStartOfflineRecording(deviceId: deviceId, dataType: dataType, settings: settings))
            .subscribe(
                onCompleted: { Napier.i("Polar360Controller: [\(dataType)] Offline recording started successfully") },
                onError: { error in Napier.e("Polar360Controller: [\(dataType)] Failed to start: \(error)") }
            )
    }

    private func resolveAndStartOfflineRecording(deviceId: String, dataType: PolarDeviceDataType, settings: PolarSensorSetting?) -> Completable {
        // PPI and HR do not accept settings in normal offline mode.
        if dataType == .ppi || dataType == .hr {
            Napier.d("Polar360Controller: [\(dataType)] Starting offline recording with nil settings")
            return polarConnector.polarApi.startOfflineRecording(deviceId, feature: dataType, settings: nil, secret: nil)
        }
        // ACC and temperature: request supported settings from the device (defaults to 2Hz for temp).
        return polarConnector.polarApi.requestOfflineRecordingSettings(deviceId, feature: dataType)
            .flatMapCompletable { resolvedSettings in
                Napier.d("Polar360Controller: [\(dataType)] Starting offline recording with settings: \(resolvedSettings)")
                return self.polarConnector.polarApi.startOfflineRecording(deviceId, feature: dataType, settings: resolvedSettings, secret: nil)
            }
    }

    func stopOfflineRecording(dataType: PolarDeviceDataType) {
        //Todo when stopping offline recording in 1 specific need to be able to restart others that are not stopped
        // SO maybe two params fetching and restart data
        guard let deviceId = currentDeviceId else { return }
        polarConnector.polarApi.stopOfflineRecording(deviceId, feature: dataType)
            .subscribe(
                onCompleted: { Napier.i("Polar360Controller: Stopped offline recording for \(dataType)") },
                onError: { error in Napier.e("Polar360Controller: Could not stop offline recording for \(dataType): \(error)") }
            )
            .disposed(by: disposeBag)
    }

    func background_offline_fetch(dataType: PolarDeviceDataType, scheduledTime : Date ,onSuccess: @escaping ([Any]) -> Void, onError: @escaping (Error) -> Void) {

    }

    func stopOfflineRecordingAndFetch(dataType: PolarDeviceDataType, onSuccess: @escaping ([Any]) -> Void, onError: @escaping (Error) -> Void) {
        // If the in-memory ID was cleared by a mid-sequence BLE disconnect while the app
        // is in the background, fall back to the value persisted in UserDefaults so the
        // remaining queued observations can still complete their fetch.
        if currentDeviceId == nil && appIsInBackground {
            restoreDeviceIdFromBackground()
        }
        guard let deviceId = currentDeviceId else {
            Napier.w("Polar360Controller: [\(dataType)] currentDeviceId is nil — returning empty")
            onSuccess([])
            return
        }
        Napier.d("Polar360Controller: [\(dataType)] Enqueueing stop+fetch for device=\(deviceId)")
        let task = buildStopAndFetch(deviceId: deviceId, dataType: dataType)
            .catch { error -> Single<[Any]> in
                Napier.e("Polar360Controller: [\(dataType)] stop+fetch failed: \(error)")
                return .just([])
            }
            .do(onSuccess: { samples in
                Napier.i("Polar360Controller: [\(dataType)] Task completed with \(samples.count) items")
                onSuccess(samples)
            })
            .asObservable()
        operationQueue.onNext(task)
    }

    private func buildStopAndFetch(deviceId: String, dataType: PolarDeviceDataType) -> Single<[Any]> {
        Napier.d("Polar360Controller: [\(dataType)] Stopping recording on device=\(deviceId)")
        return polarConnector.polarApi.stopOfflineRecording(deviceId, feature: dataType)
            .catch { error -> Completable in
                Napier.w("Polar360Controller: [\(dataType)] stopOfflineRecording API error (ignored): \(error)")
                return Completable.empty()
            }
            .do(onCompleted: {
                Napier.d("Polar360Controller: [\(dataType)] Recording stopped, listing all recordings...")
            })
            .andThen(
                polarConnector.polarApi.listOfflineRecordings(deviceId)
                    .do(onNext: { entry in
                        Napier.d("Polar360Controller: [\(dataType)] Listed entry: type=\(entry.type), date=\(entry.date), size=\(entry.size) — matches=\(entry.type == dataType)")
                    }, onError: { error in
                        Napier.e("Polar360Controller: [\(dataType)] listOfflineRecordings error: \(error)")
                    }, onCompleted: {
                        Napier.d("Polar360Controller: [\(dataType)] listOfflineRecordings completed")
                    })
                    .filter { $0.type == dataType }
                    .concatMap { [weak self] entry -> Observable<[Any]> in
                        guard let self else { return Observable.just([]) }
                        Napier.d("Polar360Controller: [\(dataType)] Fetching record: date=\(entry.date), size=\(entry.size)")
                        return self.polarConnector.polarApi.getOfflineRecord(deviceId, entry: entry, secret: nil)
                            .flatMap { [weak self] data -> Single<[Any]> in
                                guard let self else { return .just([]) }
                                Napier.d("\(data)")
                                let samples = self.extractSamples(from: data)
                                Napier.d("Polar360Controller: [\(dataType)] Fetched \(samples.count) samples, removing entry...")
                                return self.polarConnector.polarApi.removeOfflineRecord(deviceId, entry: entry)
                                    .do(onError: { error in
                                        Napier.e("Polar360Controller: [\(dataType)] removeOfflineRecord error: \(error)")
                                    }, onCompleted: {
                                        Napier.d("Polar360Controller: [\(dataType)] Entry removed")
                                    })
                                    .catch { _ in Completable.empty() }
                                    .andThen(Single.just(samples))
                            }
                            .catch { [weak self] error -> Single<[Any]> in
                                Napier.e("Polar360Controller: [\(dataType)] getOfflineRecord error: \(error) — removing unreadable entry")
                                guard let self else { return .just([]) }
                                return self.polarConnector.polarApi.removeOfflineRecord(deviceId, entry: entry)
                                    .catch { _ in Completable.empty() }
                                    .andThen(Single.just([]))
                            }
                            .asObservable()
                    }
                    .toArray()
                    .map { $0.flatMap { $0 } }
                    .do(onSuccess: { all in
                        Napier.i("Polar360Controller: [\(dataType)] Total samples returned: \(all.count)")
                    })
            )
    }

    private func extractSamples(from data: PolarOfflineRecordingData) -> [Any] {
        switch data {
        case let .accOfflineRecordingData(accData, _, _):
            return accData.map {
                acc_data(x: $0.x, y: $0.y, z: $0.z, timestamp: $0.timeStamp)
            };

        case let .ppiOfflineRecordingData(ppiData, _):
            return ppiData.samples.map {
                ppi_data(hr: $0.hr, timestamp: $0.timeStamp,
                         ppiInMs: $0.ppInMs, ppiErrorEstimate: $0.ppErrorEstimate , skinContact:  $0.skinContactStatus)
            };
            /*
             ppiData.samples.map {
                hr_data(hr: $0.hr, timestamp: $0.timeStamp)
             };*/

        case let .temperatureOfflineRecordingData(tempData, _):
            Napier.e("polarTempdata \(tempData)")
            return tempData.samples.map {
                temp_data(temp: $0.temperature, Timestamp: $0.timeStamp)
            };
        default:
            return []
        }
    }

    func isOfflineRecordingMode(config: [String: Any]) -> Bool {
        if let offlineRecording = config["Offline_recording"] {
            return String(describing: offlineRecording) == "true"
        }
        return false
    }

    func onDeviceDisconnected() {
        sdkModeEnabled = false
        currentDeviceId = nil
        // Only clear the persisted ID if we are in the foreground — a background
        // disconnect (e.g. device temporarily out of range) should not erase the
        // ID that the BGAppRefreshTask needs to reconnect and fetch data.
        if !appIsInBackground {
            clearBackgroundDeviceId()
        }
    }

    private static let backgroundDeviceIdKey = "polar360.backgroundDeviceId"

    /// Persists the current device ID to UserDefaults so it survives a cold
    /// background launch when the BGAppRefreshTask fires.
    func saveDeviceIdForBackground() {
        AppDelegate.appGroupUserDefaults?.set(currentDeviceId, forKey: Self.backgroundDeviceIdKey)
        Napier.i("Polar360Controller: saved deviceId '\(currentDeviceId ?? "nil")' for background")
    }

    /// Restores the previously saved device ID — call at the start of the
    /// BGAppRefreshTask handler so stopOfflineRecordingAndFetch can find the device.
    func restoreDeviceIdFromBackground() {
        currentDeviceId = AppDelegate.appGroupUserDefaults?.string(forKey: Self.backgroundDeviceIdKey)
        Napier.i("Polar360Controller: restored deviceId '\(currentDeviceId ?? "nil")' from background")
    }

    func clearBackgroundDeviceId() {
        AppDelegate.appGroupUserDefaults?.removeObject(forKey: Self.backgroundDeviceIdKey)
        Napier.i("Polar360Controller: cleared background deviceId")
    }

    func getPolarApi() -> PolarBleApi {
        return polarConnector.polarApi
    }

    private func enableSdkModeIfNeeded(identifier: String) -> Completable {
        if sdkModeEnabled {
            return Completable.empty()
        }
        return Completable.deferred {
            do {
                self.sdkModeEnabled = true
                return try self.polarConnector.polarApi.enableSDKMode(identifier)
            } catch {
                return Completable.empty()
            }
        }
    }

    private func disableSdkModeIfNeeded(identifier: String) -> Completable {
        if !sdkModeEnabled {
            return Completable.empty()
        }
        return Completable.deferred {
            do {
                self.sdkModeEnabled = false
                return try self.polarConnector.polarApi.disableSDKMode(identifier)
            } catch {
                return Completable.empty()
            }
        }
    }

    private func checkIfDeviceIsSetup(identifier: String) -> Completable {
        return polarConnector.polarApi.isFtuDone(identifier)
            .flatMapCompletable { ftuDone in
                if ftuDone {
                    Napier.i("Polar 360 FTU already done")
                    return Completable.empty()
                } else {
                    let dateFormatter = ISO8601DateFormatter()
                    dateFormatter.formatOptions = [.withInternetDateTime]
                    dateFormatter.timeZone = TimeZone(secondsFromGMT: 0)

                    let profile = Polar360UserProfile.load()
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

                    return self.polarConnector.polarApi
                        .doFirstTimeUse(identifier, ftuConfig: ftuConfig)
                }
            }
    }
}
