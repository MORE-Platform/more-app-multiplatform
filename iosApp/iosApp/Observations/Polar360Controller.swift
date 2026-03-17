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
    private let disposeBag = DisposeBag()

    private init() {}
    
    
    class ppi_data: Codable {
        let hr: Int
        let timestamp: UInt64
        let ppiInMs : UInt16
        let ppiErrorEstimate : UInt16
        init(hr:Int, timestamp:UInt64, ppiInMs:UInt16, ppiErrorEstimate:UInt16){
            self.hr = hr
            self.timestamp = timestamp
            self.ppiInMs = ppiInMs
            self.ppiErrorEstimate = ppiErrorEstimate
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
        init(hr:Int, timestamp:UInt64){
            self.hr = hr
            self.timestamp = timestamp
        }
    }
    
    
    func findPolar360Device() -> BluetoothDeviceEntity? {
        return bleManager.connectedDevicesValue.first { device in
            guard let name = device.deviceName?.lowercased() else { return false }
            return name.contains("polar") && name.contains("360") && device.address != nil
        }
    }

    func ensureReady(deviceId: String, offlineMode: Bool) -> Completable {
        currentDeviceId = deviceId
        return checkIfDeviceIsSetup(identifier: deviceId)
            .andThen(offlineMode
                ? disableSdkModeIfNeeded(identifier: deviceId)
                : enableSdkModeIfNeeded(identifier: deviceId)
            )
    }

    func startOfflineRecording(deviceId: String, dataType: PolarDeviceDataType, settings: PolarSensorSetting? = nil) -> Disposable {
        return resolveAndStartOfflineRecording(deviceId: deviceId, dataType: dataType, settings: settings)
            .subscribe(
                onCompleted: { NSLog("Polar360Controller: Started offline recording for \(dataType)") },
                onError: { error in NSLog("Polar360Controller: Failed to start offline recording for \(dataType): \(error)") }
            )
    }

    private func resolveAndStartOfflineRecording(deviceId: String, dataType: PolarDeviceDataType, settings: PolarSensorSetting?) -> Completable {
        guard dataType != .ppi else {
            return polarConnector.polarApi.startOfflineRecording(deviceId, feature: dataType, settings: nil, secret: nil)
        }
        return polarConnector.polarApi.requestStreamSettings(deviceId, feature: dataType)
            .catch { error -> Single<PolarSensorSetting> in
                NSLog("Polar360Controller: Stream settings request failed for \(dataType): \(error), using defaults")
                let defaultSettings = try! PolarSensorSetting([
                    .sampleRate: 1,
                    .resolution: 1
                ])
                return Single.just(defaultSettings)
            }
            .flatMapCompletable { resolvedSettings in
                NSLog("Polar360Controller: Using settings for \(dataType) offline recording: \(resolvedSettings)")
                return self.polarConnector.polarApi.startOfflineRecording(deviceId, feature: dataType, settings: resolvedSettings, secret: nil)
            }
    }

    func stopOfflineRecording(dataType: PolarDeviceDataType) {
        //Todo when stopping offline recording in 1 specific need to be able to restart others that are not stopped
        // SO maybe two params fetching and restart data
        guard let deviceId = currentDeviceId else { return }
        polarConnector.polarApi.stopOfflineRecording(deviceId, feature: dataType)
            .subscribe(
                onCompleted: { NSLog("Polar360Controller: Stopped offline recording for \(dataType)") },
                onError: { error in NSLog("Polar360Controller: Could not stop offline recording for \(dataType): \(error)") }
            )
            .disposed(by: disposeBag)
    }

    func stopOfflineRecordingAndFetch(dataType: PolarDeviceDataType, onSuccess: @escaping ([Any]) -> Void, onError: @escaping (Error) -> Void) {
        guard let deviceId = currentDeviceId else {
            onSuccess([])
            return
        }
        polarConnector.polarApi.stopOfflineRecording(deviceId, feature: dataType)
            .andThen(
                polarConnector.polarApi.listOfflineRecordings(deviceId)
                    .filter { $0.type == dataType }
                    .concatMap { [weak self] entry -> Observable<[Any]> in
                        guard let self else { return Observable.just([]) }
                        NSLog("Polar360Controller: Fetching record entry=\(entry)")
                        return self.polarConnector.polarApi.getOfflineRecord(deviceId, entry: entry, secret: nil)
                            .flatMap { data -> Single<[Any]> in
                                let samples = self.extractSamples(from: data)
                                NSLog("Polar360Controller: Fetched \(samples.count) samples from entry=\(entry)")
                                return self.polarConnector.polarApi.removeOfflineRecord(deviceId, entry: entry)
                                    .andThen(Single.just(samples))
                            }
                            .catch { error -> Single<[Any]> in
                                NSLog("Polar360Controller: Skipping entry \(entry), failed to parse: \(error)")
                                return .just([])
                            }
                            .asObservable()
                    }
                    .toArray()
                    .map { $0.flatMap { $0 } }
            )
            .subscribe(
                onSuccess: { samples in
                    NSLog("Polar360Controller: stopOfflineRecordingAndFetch succeeded with \(samples.count) total samples for \(dataType)")
                    onSuccess(samples)
                },
                onError: { error in
                    NSLog("Polar360Controller: stopOfflineRecordingAndFetch failed for \(dataType): \(error)")
                }
            )
            .disposed(by: disposeBag)
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
                         ppiInMs: $0.ppInMs, ppiErrorEstimate: $0.ppErrorEstimate)
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
                    NSLog("Polar 360 FTU already done")
                    return Completable.empty()
                } else {
                    let dateFormatter = ISO8601DateFormatter()
                    dateFormatter.formatOptions = [.withInternetDateTime]
                    dateFormatter.timeZone = TimeZone(secondsFromGMT: 0)

                    let ftuConfig = PolarFirstTimeUseConfig(
                        gender: PolarFirstTimeUseConfig.Gender.female,
                        birthDate: Date(),
                        height: 180,
                        weight: 80,
                        maxHeartRate: 180,
                        vo2Max: 80,
                        restingHeartRate: 100,
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
