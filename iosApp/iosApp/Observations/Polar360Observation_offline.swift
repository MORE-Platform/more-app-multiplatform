//
//  Polar360Observation.swift
//  iosApp
//
//  Created by Masek Gergely on 26.09.25.
//  Copyright © 2025 Redlink GmbH. All rights reserved.
//

import CoreBluetooth
import Foundation
import PolarBleSdk
import RxSwift
import shared
import UIKit
import Combine
import KMPNativeCoroutinesCombine

class Polar360Observation_offline: Observation_{

    

    private let deviceIdentificer: Set<String> = ["Polar"]
    private let polarConnector = AppDelegate.polarConnector
    private let disposeBag = DisposeBag( )
    private var connectedDevices: [BluetoothDeviceEntity] = []
    private var hrObservation: Disposable?
    private var tmpObservation: Disposable?
    private var accObservation: Disposable?
    private var ppiObservation: Disposable?

    private let deviceManager = BluetoothStateManagement.shared

    private var deviceListener: AnyCancellable?

    private let errorStringTable = "Errors"
    private var deviceid : String? = nil
    private let schedulerForeground = ConcurrentDispatchQueueScheduler(qos:.userInitiated)
    private let schedulerBackground = ConcurrentDispatchQueueScheduler(qos: .background)


    init (repos: MainRepository , sensorPermissions: Set<String>){
        super.init(repos: repos, observationType: Polar360OfflineType(sensorPermissions: sensorPermissions))
    }

    class hrData: Codable {
        let hr: Int
        let timestamp: UInt64

        init(hr:Int, timestamp:UInt64){
            self.hr = hr
            self.timestamp = timestamp
        }
    }

    class accData: Codable {
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

    class tempData: Codable {
        let temp: Float
        let timestamp: UInt64

        init(temp:Float , Timestamp:UInt64){
            self.temp = temp
            self.timestamp = Timestamp
        }
    }

    class SyncedPacket: Codable {
        let hr_data: hrData
        let acc_data : accData?
        let temp_data: tempData
        init(hr_data: hrData, acc_data: accData?, temp_data: tempData) {
            self.hr_data = hr_data
            self.acc_data = acc_data
            self.temp_data = temp_data
        }
        /// Convert the packet into a JSON string
        func toJson(pretty: Bool = false) -> String? {
            let encoder = JSONEncoder()
            if pretty {
                encoder.outputFormatting = .prettyPrinted
            }

            do {
                let data = try encoder.encode(self)
                return String(data: data, encoding: .utf8)
            } catch {
                print("❌ Failed to encode SyncedPacket: \(error)")
                return nil
            }
        }

        /// Convert the packet into raw Data (for networking, file storage, etc.)
        func toJsonData(pretty: Bool = false) -> Data? {
            let encoder = JSONEncoder()
            if pretty {
                encoder.outputFormatting = .prettyPrinted
            }

            do {
                return try encoder.encode(self)
            } catch {
                print("❌ Failed to encode SyncedPacket: \(error)")
                return nil
            }
        }

    }


    private let hrQueue = Polar360Queue<hrData>(maxSize: 10)
    private let accQueue = Polar360Queue<accData>(maxSize: 10)
    private let tempQueue = Polar360Queue<tempData>(maxSize: 10)


    private func tryBuildPacket()-> SyncedPacket?{
        if((hrQueue.size() != 0) && (tempQueue.size() != 0 ))
        {
            let hritem = hrQueue.pollLast()
            let tempitem = tempQueue.pollLast()
            let accitem = accQueue.pollLast() ?? nil
            if (hritem != nil && tempitem != nil ){
                return SyncedPacket(hr_data: hritem!, acc_data: accitem, temp_data: tempitem!)
            }
        }
        return nil
    }

    private func sendOutData(packet:SyncedPacket){
        print("sending data \(packet)")
        print("packet data \(packet.hr_data.hr) \(packet.temp_data.temp) ")

        let data = [
            "hr": [
                "value": packet.hr_data.hr,
                "timestamp": packet.hr_data.timestamp
            ],
            "acc": [
                "x":   packet.acc_data?.x ?? 0,
                "y":packet.acc_data?.y ?? 0,
                "z": packet.acc_data?.z ?? 0,
                "timestamp": packet.acc_data?.timestamp ?? 0
            ],
            "temp": [
                "value": packet.temp_data.temp,
                "timestamp": packet.temp_data.timestamp
            ]
        ]

        self.storeData(data: data, timestamp: -1){
            print("stored")
        }

    }
    
    override func ableToAutomaticallyStart() -> Bool {
        return false
    }

    override func start() -> Bool {
        if observerAccessible() {
            let acceptableDevices = deviceManager.connectedDevicesValue.deviceWithNameIn(nameSet: deviceIdentificer)
            
            if !acceptableDevices.isEmpty, let firstAddress = acceptableDevices[0].address {
                deviceid = firstAddress
                polarConnector.polarApi.disableSDKMode(firstAddress).subscribe(
                    onCompleted: {
                        print("✅ SDK mode disabled")
                    },
                    onError: { error in
                        print("⚠️ Failed to disable SDK mode: \(error)")
                    }
                )
                .disposed(by: disposeBag)
                
                setupForFirstTimeUse(identifier: firstAddress)
                    .andThen(
                        self.setupOfflineRecording(identifier: firstAddress)
                    )
                    .subscribe(
                        onSuccess: { returnval in
                            print("offline recording started")
                        },
                        onFailure: { error in
                            print("⚠️ Error: \(error)")
                        }
                    )
                    .disposed(by: disposeBag)
                self.polarConnector.polarApi.getDiskSpace(firstAddress).subscribe(
                    onSuccess: {
                        memory in print("Memory: \(memory)")
                    },
                    onError: { error in
                        print("issue fetching memory")
                    }
                ).disposed(by: disposeBag)
                self.polarConnector.polarApi.requestOfflineRecordingSettings(firstAddress, feature: .temperature).subscribe(
                    onSuccess: {
                        settings in
                        self.polarConnector.polarApi.startOfflineRecording(firstAddress,  feature: .temperature ,settings:settings,secret: nil).subscribe(
                            onCompleted: {
                                print("✅ Started offline recording")
                            },
                            onError: { error in
                                print("⚠️ Error starting offline recording: \(error)")
                            }
                        )
                    },
                    onError : { error in
                        print("error getting settings \(error)")
                    }
                )
                self.polarConnector.polarApi.requestOfflineRecordingSettings(firstAddress, feature: .acc).subscribe(
                    onSuccess: {
                        settings in
                        self.polarConnector.polarApi.startOfflineRecording(firstAddress, feature: .acc, settings: settings, secret: nil).subscribe(
                            onCompleted: {
                                print("Started offline recording acc")
                            },
                            onError: {
                                error in
                                print("error w acc offline recording \(error)")
                            }
                        )
                    },
                    onError: {
                        error in
                        print("Error starting acc offline recording \(error)")
                    }
                    
                )
            }
            return true
        }
        
    

        print("obsv ended")
        return false
    }
    

    override func stop(onCompletion: @escaping () -> Void) {
        let acceptableDevices = deviceManager.connectedDevicesValue.deviceWithNameIn(nameSet: deviceIdentificer)
        
        if !acceptableDevices.isEmpty, let firstAddress = acceptableDevices[0].address{
            self.polarConnector.polarApi
                .listOfflineRecordings(firstAddress)
                .subscribe(
                    onNext: { entry in
                        print("📦 Found recording: \(entry.path) (\(entry.type))")

                        // Now download this recording
                        self.polarConnector.polarApi
                            .getOfflineRecord(firstAddress, entry: entry, secret: nil)
                            .subscribe(onSuccess: { data in
                                switch data {

                                // Temperature case — note two-tuple pattern (tempData, startTime)
                                case .temperatureOfflineRecordingData(let tempData, let startTime):
                                    // tempData: (timeStamp: UInt64, samples: [(timeStamp: UInt64, temperature: Float)])
                                    print("🌡️ Temperature recording starting at \(startTime)")
                                    print("base timestamp: \(tempData.timeStamp)")
                                    print("samples count: \(tempData.samples.count)")

                                    for sample in tempData.samples {
                                        // sample.timeStamp is UInt64 (ms since epoch) per your type
                                        
                                        print("• \(sample.timeStamp): \(sample.temperature) °C")
                                    }

                                // Skin temperature — often same shape as temperature but listed separately in your enum list
                                case .skinTemperatureOfflineRecordingData(let skinTempData, let startTime):
                                    print("🧴 Skin temp from \(startTime), samples: \(skinTempData.samples.count)")
                                    for sample in skinTempData.samples {
                                      
                                        print("• \(sample.timeStamp): \(sample.temperature) °C")
                                    }

                                // ACC case — this one (per your earlier listing) carries (accData, startTime, settings)
                                case .accOfflineRecordingData(let accData, let startTime, let settings):
                                    // accData likely: (timeStamp: UInt64, samples: [(timeStamp: UInt64, x: Float, y: Float, z: Float)])
                                    print("📈 ACC recording from \(startTime) — settings: \(settings)")
                                    for sample in accData {
                                        let sampleDate = Date(timeIntervalSince1970: Double(sample.timeStamp) / 1000.0)
                                        print("• \(sample.timeStamp): x=\(sample.x), y=\(sample.y), z=\(sample.z)")
                                    }

                                // Gyro (3-tuple: data, startTime, settings)
                                case .gyroOfflineRecordingData(let gyroData, let startTime, let settings):
                                    print("🌀 Gyro from \(startTime) — settings: \(settings)")
                                    for sample in gyroData {
                                        let sampleDate = Date(timeIntervalSince1970: Double(sample.timeStamp) / 1000.0)
                                        print("• \(sampleDate): x=\(sample.x), y=\(sample.y), z=\(sample.z)")
                                    }

                                // Mag (3-tuple)
                                case .magOfflineRecordingData(let magData, let startTime, let settings):
                                    print("🧭 Mag from \(startTime) — settings: \(settings)")
                                    for sample in magData {
                                        let sampleDate = Date(timeIntervalSince1970: Double(sample.timeStamp) / 1000.0)
                                        print("• \(sampleDate): x=\(sample.x), y=\(sample.y), z=\(sample.z)")
                                    }

                                // PPG (3-tuple)
                                case .ppgOfflineRecordingData(let ppgData, let startTime, let settings):
                                    print("❤️ PPG from \(startTime) — samples: \(ppgData.samples.count) settings: \(settings)")
                                    // ppgData.samples shape depends on SDK — print a small sample
                                    for s in ppgData.samples.prefix(5) {
                                        let sampleDate = Date(timeIntervalSince1970: Double(s.timeStamp) / 1000.0)
                                        print("• \(sampleDate): sample len / values: \(s)")
                                    }

                                // PPI (2-tuple: samples, startTime)
                                case .ppiOfflineRecordingData(let ppiData, let startTime):
                                    print("💓 PPI from \(startTime)")
                                    
                                // HR (2-tuple)
                                case .hrOfflineRecordingData(let hrData, let startTime):
                                    print("💗 HR from \(startTime)")
                                    
                                case .emptyData(let startTime):
                                    print("⚪ Empty data at \(startTime)")
                                }
                            }, onError: { error in
                                print("⚠️ Error reading \(entry.path): \(error)")
                            })
                            .disposed(by: self.disposeBag)

                    },
                    onError: { error in
                        print("⚠️ Error listing recordings: \(error)")
                    },
                    onCompleted: {
                        print("✅ All recordings processed")
                    }
                )
                .disposed(by: self.disposeBag)


            
            self.polarConnector.polarApi.fetchStoredExerciseList(firstAddress).subscribe(
                onNext: { exercise in
                    print(exercise)
                    // exercise is a tuple (path, date, entryId)
                    print("Path:", exercise.path)
                    print("Date:", exercise.date.description)
                    print("Entry ID:", exercise.entryId)
                },
                onError: { error in
                    print("Error fetching exercises:", error)
                },
                onCompleted: {
                    print("All stored exercises fetched.")
                }
            )
            .disposed(by: disposeBag)
            deviceListener?.cancel()
            onCompletion()
        }}

    private func setupForFirstTimeUse(identifier: String) -> Completable {


        return polarConnector.polarApi.isFtuDone(identifier)
        .flatMapCompletable { ftuDone in
            if ftuDone {
                NSLog("FTU already done")
                return Completable.empty() // nothing to do
            } else {
                let dateFormatter = ISO8601DateFormatter()
                dateFormatter.formatOptions = [.withInternetDateTime]
                dateFormatter.timeZone = TimeZone(secondsFromGMT: 0)

                let ftuConfig = PolarFirstTimeUseConfig(
                    gender: PolarFirstTimeUseConfig.Gender.female,
                    birthDate: Date(),
                    height: 180,
                    weight: 80,
                    maxHeartRate: 100,
                    vo2Max: 80,
                    restingHeartRate: 20,
                    trainingBackground: PolarFirstTimeUseConfig.TrainingBackground.frequent,
                    deviceTime: dateFormatter.string(from: Date()),
                    typicalDay: PolarFirstTimeUseConfig.TypicalDay.mostlyMoving,
                    sleepGoalMinutes: 480
                )

                return self.polarConnector.polarApi
                    .doFirstTimeUse(identifier, ftuConfig: ftuConfig)
            }
        }
        .andThen(
            Completable.deferred {
                return Completable.empty()
                //return  self.polarConnector.polarApi.enableSDKMode(identifier)


            }
        )
    }
    
    

    private func enableSdkMode(identifier: String) -> Completable {
        print("🟡 enableSdkMode() called for \(identifier)")

        return polarConnector.polarApi
            .isSDKModeEnabled(identifier)
            .flatMapCompletable { [weak self] sdkEnabled -> Completable in
                guard let self = self else { return .empty() }

                if sdkEnabled {
                    print("ℹ️ SDK mode already enabled for \(identifier)")
                    return .empty() // Already enabled
                } else {
                    print("⚙️ Enabling SDK mode for \(identifier)...")
                    return self.polarConnector.polarApi
                        .enableSDKMode(identifier)
                        .do(
                            onCompleted: { print("✅ SDK mode enabled for \(identifier)") },
                            onSubscribe: { print("🔄 Subscribing to enableSDKMode...") },
                            onDispose: { print("🧹 Disposed enableSDKMode()") }
                        )
                }
            }
    }
    private func setupOfflineRecording(identifier:String) -> Single<Bool> {
        return polarConnector.polarApi.setAutomaticTrainingDetectionSettings(
            identifier,
            mode:true,
            sensitivity:100 ,
            minimumDuration:300,
        ).andThen(Single.just(true))
    }
    
    private func startOfflineRecording(
        identifier: String,
        feature: PolarDeviceDataType
    ) -> Single<Bool> {
        print("ℹ️ Starting offline recording for feature: \(feature)")

        return polarConnector.polarApi
            .requestOfflineRecordingSettings(identifier, feature: feature)
            .catch { error -> Single<PolarSensorSetting> in
                print("⚠️ Failed to fetch recording settings, using defaults:", error)
                let defaultSettings = try! PolarSensorSetting([
                    .sampleRate: 1,
                    .resolution: 1
                ])
                print("ℹ️ Using default settings: \(defaultSettings)")
                return Single.just(defaultSettings)
            }
            .flatMap { sensorSettings -> Single<Bool> in
                print("ℹ️ Attempting to start offline recording with settings: \(sensorSettings)")

                return self.polarConnector.polarApi
                    .startOfflineRecording(
                        identifier,
                        feature: feature,
                        settings: sensorSettings,
                        secret: nil
                    )
                    .do(onError: { error in
                        print("❌ Failed to start offline recording for feature \(feature):", error)
                    },onCompleted: {
                        print("✅ Successfully started offline recording for feature: \(feature)")
                    }, )
                    .andThen(Single.just(true))
            }
    }
    
    
    
    private func listenToDeviceConnection() {
        deviceListener = createPublisher(for: deviceManager.connectedDevices)
            .removeDuplicates()
            .receive(on: DispatchQueue.main)
            .sink(receiveCompletion: { _ in }, receiveValue: { [weak self] devices in
                if let self, !self.deviceIdentificer.anyNameIn(items: devices) {
                    PolarStates.shared.hrFeatureReady(ready: false)
                    self.deviceListener?.cancel()
                }
            })
    }
    override func applyObservationConfig(settings: Dictionary<String, Any>) {
    }
}
