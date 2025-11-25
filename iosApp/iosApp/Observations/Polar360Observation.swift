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

class Polar360Observation: Observation_{

    

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
    private var samplingrate : Int = 1

    private let OfflineRecording : Bool = false
    
    init (repos: MainRepository , sensorPermissions: Set<String>){
        super.init(repos: repos, observationType: Polar360Type(sensorPermissions: sensorPermissions))
    }

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
    
    class OfflineDataPack: Codable {
        var hr_data: [hr_data]?
        var ppi_data: [ppi_data]?
        var temp_data: [temp_data]?
        var acc_data: [acc_data]?

        init(
            hr_data: [hr_data]?,
            ppi_data: [ppi_data]?,
            temp_data: [temp_data]?,
            acc_data: [acc_data]?
        ) {
            self.hr_data = hr_data
            self.ppi_data = ppi_data
            self.temp_data = temp_data
            self.acc_data = acc_data
        }
    }

    class SyncedPacket: Codable {
        let ppi_data: ppi_data
        let acc_data : [acc_data]?
        let temp_data: temp_data
        init(ppi_data: ppi_data, acc_data: [acc_data]?, temp_data: temp_data) {
            self.ppi_data = ppi_data
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


    private let hrQueue = Polar360Queue<ppi_data>(maxSize: 10)
    private let accQueue = Polar360Queue<[acc_data]>(maxSize: 10)
    private let tempQueue = Polar360Queue<temp_data>(maxSize: 10)


    private func tryBuildPacket()-> SyncedPacket?{
        if((hrQueue.size() != 0) && (tempQueue.size() != 0 ) && (accQueue.size() != 0 ))
        {
            let ppi_item = hrQueue.pollLast()
            let tempitem = tempQueue.pollLast()
            let accitem = accQueue.pollLast()
            if (ppi_item != nil && tempitem != nil && accitem != nil ){
                return SyncedPacket(ppi_data: ppi_item!, acc_data: accitem, temp_data: tempitem!)
            }
        }
        return nil
    }

    private func sendOutData(packet:SyncedPacket){
        print("sending data \(packet)")
        print("packet data \(packet.ppi_data.hr) \(packet.temp_data.temp) ")
        
        
        
        
        let data = [
            "hr": [
                "value": packet.ppi_data.hr,
                "timestamp": packet.ppi_data.timestamp,
                "ppiInMs" : packet.ppi_data.ppiInMs,
                "ppiErrorEstimate" : packet.ppi_data.ppiErrorEstimate
            ],
            "acc": packet.acc_data?.compactMap({ accSample in
                [
                    "x": accSample.x,
                    "y": accSample.y,
                    "z": accSample.z,
                    "timestamp": accSample.timestamp
                ]
            }),
            "temp": [
                "value": packet.temp_data.temp,
                "timestamp": packet.temp_data.timestamp
            ]
        ] as [String : Any]

        self.storeData(data: data, timestamp: -1){
            print("Data stored sending to backend")
        }

    }


    override func start() -> Bool {
        if observerAccessible() {
            let acceptableDevices = deviceManager.connectedDevicesValue.deviceWithNameIn(nameSet: deviceIdentificer)

            if !acceptableDevices.isEmpty, let firstAddress = acceptableDevices[0].address {
                deviceid = firstAddress
                
                
                if(OfflineRecording){
                    self.polarConnector.polarApi.disableSDKMode(firstAddress).subscribe(
                        onCompleted: { [weak self] in
                            guard let self else { return }
                            print("Disabled SDK Mode for offline recording")
                        },
                        onError: {
                            error in
                            print("Error: \(error)")
                        }
                    ).disposed(by: disposeBag)
                    
                    setupForFirstTimeUse(identifier: firstAddress).subscribe(
                        onCompleted: { [weak self] in
                            guard let self else { return }
                            self.polarConnector.polarApi.startOfflineRecording(firstAddress,feature: .hr,settings: nil,secret: nil).subscribe(
                                onCompleted: {
                                    print("Started Offline Recording for hr")
                                },
                                onError: { error in
                                    print("Error: \(error)")
                                }
                            ).disposed(by: disposeBag)
                            self.polarConnector.polarApi.startOfflineRecording(firstAddress,feature: .acc,settings: nil,secret: nil)
                            self.polarConnector.polarApi.startOfflineRecording(firstAddress,feature: .ppi,settings: nil,secret: nil)
                            self.polarConnector.polarApi.startOfflineRecording(firstAddress,feature: .temperature,settings: nil,secret: nil)
                        },
                        onError: {
                            error in
                            print("Error: \(error)")
                        }
                    ).disposed(by: disposeBag)
                    
                }
                else{
                    self.polarConnector.polarApi.enableSDKMode(firstAddress).subscribe(
                        onCompleted: { [weak self] in
                            guard let self else { return }
                            print("Enabled SDK Mode")
                        },
                        onError: {
                            error in
                            print("Error: \(error)")
                        }
                    ).disposed(by: disposeBag)
                    
                    
                    setupForFirstTimeUse(identifier: firstAddress)
                        .subscribe(
                            onCompleted: { [weak self] in
                                guard let self else { return }
                                self.listenToDeviceConnection()
                                print("Starting streaming...")
                                //self.hrObservation = self.hrstream(identifier: firstAddress, scheduler: self.schedulerBackground)
                                self.ppiObservation = self.ppistream(identifier: firstAddress, scheduler: self.schedulerBackground)
                                self.accObservation = self.accstream(identifier: firstAddress, scheduler: self.schedulerBackground)
                                self.tmpObservation = self.tmpstream(identifier: firstAddress, scheduler: self.schedulerBackground)
                            },
                            onError: { error in
                                print("Setup failed with error: \(error)")
                            }
                        )
                        .disposed(by: disposeBag)
                    
                }
            }
            return true
        }

        showObservationErrorNotification(notificationBody:"Cannot start Observation! Please make sure to enable Bluetooth and connect all necessary devices!",fallbackTitle: "Observation ERROR" )
        return false
    }
    

    override func stop(onCompletion: @escaping () -> Void) {

        self.polarConnector.polarApi.stopOfflineRecording(self.deviceid ?? "" , feature: .acc)
        .subscribe(
            onCompleted: {
                print("stopped acc")
            }
        ).disposed(by: disposeBag)
        self.polarConnector.polarApi.stopOfflineRecording(self.deviceid ?? "" , feature: .temperature)
            .subscribe(onCompleted: {print("stopped temp")})
            .disposed(by: disposeBag)

        self.polarConnector.polarApi.stopOfflineRecording(self.deviceid ?? "", feature: .ppi)
            .subscribe(onCompleted: {print("stopped ppi")})
            .disposed(by: disposeBag)
        if(OfflineRecording){
            var recordings: [PolarOfflineRecordingEntry] = []
            self.polarConnector.polarApi.listOfflineRecordings(self.deviceid ?? "")
                .subscribe(
                        onNext: { entry in
                            print("--- Offline Recording Entry ---")
                            print("id: \(entry.path)")
                            print("type: \(entry.type)")
                            print("start: \(entry.date)")
                            print("stop: \(entry.size)")
                            recordings.append(entry)
                        },
                        onError: { error in
                            print("Error: \(error)")
                        },
                        onCompleted: {
                            print("Done")
                        }
                    )
            let pack = OfflineDataPack(hr_data:nil,ppi_data:nil,temp_data:nil,acc_data:nil)
            for recording in recordings {

                switch recording.type {

                case .acc:
                    // Fetch ACC data
                    polarConnector.polarApi
                        .getOfflineRecord(self.deviceid ?? "", entry: recording, secret: nil)
                        .subscribe(onSuccess: { data in
                            print(data)
                            // Assuming `data` contains ACC samples in `data.accSamples` or raw bytes
                            // You need to parse ACC samples here
                            // Example pseudo-code:
                            // for sample in data.accSamples { pack.acc_data.append(AccData(x: sample.x, y: sample.y, z: sample.z, timestamp: sample.timestamp)) }
                        }, onError: { error in
                            print("Error fetching ACC: \(error)")
                        })

                case .ppi:
                    // Fetch PPI data
                    polarConnector.polarApi
                        .getOfflineRecord(self.deviceid ?? "", entry: recording, secret: nil)
                        .subscribe(onSuccess: { data in
                            print(data)
                            // Parse PPI samples and append
                            // for sample in data.ppiSamples { pack.ppi_data.append(PpiData(ppi: sample.ppi, timestamp: sample.timestamp)) }
                        }, onError: { error in
                            print("Error fetching PPI: \(error)")
                        })

                case .hr:
                    // Fetch HR data
                    polarConnector.polarApi
                        .getOfflineRecord(self.deviceid ?? "", entry: recording, secret: nil)
                        .subscribe(onSuccess: { data in
                            print(data)
                            // Parse HR samples and append
                            // for sample in data.hrSamples { pack.hr_data.append(HrData(bpm: sample.hr, timestamp: sample.timestamp)) }
                        }, onError: { error in
                            print("Error fetching HR: \(error)")
                        })

                default:
                    print("Unsupported type, skipping")
                }
            }
            
            
        }
        else{
            self.ppiObservation?.dispose()
            self.accObservation?.dispose()
            self.hrObservation?.dispose()
            deviceListener?.cancel()
            onCompletion()}
    }
    
    

    private func accstream(identifier:String,scheduler: ConcurrentDispatchQueueScheduler)->Disposable?{
        
        return polarConnector.polarApi
        .requestStreamSettings(identifier, feature: .acc)
        .catch { error -> Single<PolarSensorSetting> in
            print("ACC settings request failed: \(error)")
           
            let defaultSettings = try! PolarSensorSetting([
                                                              .sampleRate: 50,
                                                              .resolution: 1
                                                          ])
            return Single.just(defaultSettings)
        }
        .asObservable()
        .flatMap { settings in
            
            return self.polarConnector.polarApi.startAccStreaming(identifier, settings: settings)
        }
        .subscribe(on: MainScheduler.instance)
        .observe(on: scheduler)
        .subscribe(
            onNext: { data  in
                
               
                let data_formatted: [acc_data] = data.map { sample in
                    acc_data(x: sample.x, y: sample.y, z: sample.z, timestamp: sample.timeStamp)
                }
                if self.accQueue.size() == 0
                {
                    self.accQueue.add(data_formatted)
                }
                else {
                    var items = self.accQueue.pollLast()!
                    items.append(contentsOf: data_formatted)
                    self.accQueue.add(items)
                    
                }
                guard let sample = data.first else { return }
               
            },
            onError: { error in
                print("Accelerometer stream failed: \(error)")
            },
            onCompleted: {
                print("Accelerometer stream finished")
            }
        )
    }
    
    //Not using hr stream using ppi instead
    private func hrstream(identifier:String,scheduler: ConcurrentDispatchQueueScheduler)->Disposable?{
        return polarConnector.polarApi.startHrStreaming(identifier)
            .subscribe(on: scheduler)
            .throttle(.seconds(samplingrate), scheduler: scheduler) //This allows us to lower the sampling rate to anything we want,
            .subscribe(onNext: { [weak self] data in
                if let self, let hrData = data.first {
                    
                    
                    self.storeData(data: ["hr": hrData.hr], timestamp: -1) {}

                }
            }, onError: { [weak self] error in
                print("\(error) hr stream fail error")
                if let self {
                    showObservationErrorNotification(notificationBody: "Error continuing Observation! There was a connection issue to a bluetooth sensor. Please make sure to enable bluetooth and connect all necessary devices!", fallbackTitle: "Observation Error")
                    Observation_.pauseObservation(self.observationType)
                }
            })
    }
    private func ppistream(identifier:String,scheduler: ConcurrentDispatchQueueScheduler)->Disposable?{
        return polarConnector.polarApi.startPpiStreaming(identifier)
            .subscribe(on:scheduler)
            .throttle(.seconds(samplingrate), scheduler: scheduler)
            .subscribe(onNext: { data in
                if let sample = data.samples.first {
                    
                    self.hrQueue.add(ppi_data(hr: sample.hr,timestamp: sample.timeStamp,ppiInMs: sample.ppInMs , ppiErrorEstimate: sample.ppErrorEstimate))
                    self.tryBuildPacket().map {
                        packet in
                        //dont want to fill up queue with other streams untill hr data starts arriving
                        // so we store state and check for it

                        self.sendOutData(packet: packet)
                    }
                    print("ppi sample \(sample) " )
                }
            })
    }
    private func tmpstream(identifier:String,scheduler: ConcurrentDispatchQueueScheduler)->Disposable?{
        return polarConnector.polarApi
        .requestStreamSettings(identifier, feature: .temperature)
        .catch { error -> Single<PolarSensorSetting> in
            print("Temperature settings request failed: \(error)")
            let defaultSettings = try! PolarSensorSetting([
                                                              .sampleRate: 1,
                                                              .resolution: 1
                                                          ])
            return Single.just(defaultSettings)
        }
        .asObservable()
        .flatMap { settings in
            print("Using temperature settings: \(settings)")
            
            return self.polarConnector.polarApi.startTemperatureStreaming(identifier, settings: settings)
        }
        .subscribe(on: MainScheduler.instance) // start on main (required by Polar SDK)
        .observe(on: scheduler)
        .throttle(.seconds(samplingrate), scheduler: scheduler)
        .subscribe(onNext: { data in
            if let sample = data.samples.first {
                if(self.hrQueue.peekLast() != nil){
                    self.tempQueue.add(temp_data(temp: sample.temperature, Timestamp: sample.timeStamp))

                    self.tryBuildPacket().map {
                        packet in
                        self.sendOutData(packet: packet)
                    }}
                print("\(sample.timeStamp): \(sample.temperature)")
            }
        },
                   onError: {error in print("Error: \(error)")})
    }


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
                    maxHeartRate: 180,
                    vo2Max: 40,
                    restingHeartRate: 80,
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
                print("FTU setup successful")
                return Completable.empty()
                //return  self.polarConnector.polarApi.enableSDKMode(identifier)


            }
        )
    }

    private func enableSdkMode(identifier: String) -> Disposable {
        return polarConnector.polarApi
            .isSDKModeEnabled(identifier)
            .subscribe(
                onSuccess: { [weak self] sdkEnabled in
                    guard let self = self else { return }

                    if !sdkEnabled {
                        _ = self.polarConnector.polarApi
                            .enableSDKMode(identifier)
                            .subscribe(
                                onCompleted: {
                                    print("✅ SDK mode enabled for \(identifier)")
                                },
                                onError: { error in
                                    print("❌ Failed to enable SDK mode: \(error)")
                                }
                            )
                    } else {
                        print("ℹ️ SDK mode already enabled for \(identifier)")
                    }
                },
                onFailure: { error in
                    print("❌ Failed to check SDK mode: \(error)")
                }
            )
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
        if let value = settings["sampling_rate"] {
                // KMM numeric objects often respond to `intValue` or `doubleValue`
                if let kotlinNumber = value as? NSNumber {
                    samplingrate = kotlinNumber.intValue
                } else {
                    // Try casting to AnyObject and use description -> Int
                    let stringValue = String(describing: value)
                    if let intValue = Int(stringValue) {
                        samplingrate = intValue
                    } else {
                        print("Warning: sampling_rate is not a valid number: \(value)")
                    }
                }
            }
    }
}
