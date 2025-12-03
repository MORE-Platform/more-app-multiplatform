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

    private var OfflineRecording : Bool = false
    private var Acc : Bool = false
    private var Ppi : Bool = false
    private var Hr : Bool = false
    private var Tmp : Bool = false
    private var Continous_recording : Bool = false
    
    init (repos: MainRepository , sensorPermissions: Set<String>){
        super.init(repos: repos, observationType: Polar360Type(sensorPermissions: sensorPermissions))
    }
    override func ableToAutomaticallyStart() -> Bool {
        return false
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
        func toJSON() -> [String: Any]? {
                do {
                    let encoder = JSONEncoder()
                    let data = try encoder.encode(self) // Encode to Data
                    let jsonObject = try JSONSerialization.jsonObject(with: data, options: [])
                    return jsonObject as? [String: Any] // Convert to dictionary
                } catch {
                    print("Error converting OfflineDataPack to JSON: \(error)")
                    return nil
                }
            }

            /// Optional: Pretty-print JSON as a string
            func toJSONString(pretty: Bool = true) -> String? {
                guard let jsonDict = toJSON() else { return nil }
                do {
                    let options: JSONSerialization.WritingOptions = pretty ? .prettyPrinted : []
                    let data = try JSONSerialization.data(withJSONObject: jsonDict, options: options)
                    return String(data: data, encoding: .utf8)
                } catch {
                    print("Error converting JSON dictionary to string: \(error)")
                    return nil
                }
            }
    }

    class SyncedPacket: Codable {
        var hr_data : hr_data?
        var ppi_data: ppi_data?
        var acc_data : [acc_data]?
        var temp_data: temp_data?
        init(hr_data : hr_data? ,ppi_data: ppi_data?, acc_data: [acc_data]?, temp_data: temp_data?) {
            self.hr_data = hr_data
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


    private let hrQueue = Polar360Queue<hr_data>(maxSize: 20)
    private let ppiQueue = Polar360Queue<ppi_data>(maxSize: 20)
    private let accQueue = Polar360Queue<[acc_data]>(maxSize: 20)
    private let tempQueue = Polar360Queue<temp_data>(maxSize: 20)


    private func tryBuildPacket() -> SyncedPacket? {
        // Only poll queues if the corresponding flag is true
        let hrData  = Hr  ? hrQueue.peekLast()  : nil
        let tmpData = Tmp ? tempQueue.peekLast() : nil
        let ppiData = Ppi ? ppiQueue.peekLast()  : nil
        let accData = Acc ? accQueue.peekLast()  : nil
        
        
        // If all enabled flags resulted in nil → return nil
        if (Hr && hrData == nil) || (Tmp && tmpData == nil) || (Ppi && ppiData == nil) {
                print("Build pack failed")
               return nil
           }
        hrQueue.pollLast()
        ppiQueue.pollLast()
        tempQueue.pollLast()
        accQueue.pollLast()
        
        return SyncedPacket(
                hr_data: hrData,
                ppi_data: ppiData,
                acc_data: accData,
                temp_data: tmpData
        )
        
    }
    
    
    
    private func sendOutData(packet:SyncedPacket){
        print("sending data \(packet)")
        var data = [:] as [String:Any]
        if(Hr ){
            if( packet.hr_data != nil){
                data["hr"]=[
                    "value": packet.hr_data?.hr,
                    "timestamp": packet.hr_data?.timestamp,
                ]}
            else {
                data["hr"]=[
                    "value": 0,
                    "timestamp": 0,
                ]
            }
        }
        if(Ppi)
        {
            if (packet.ppi_data != nil){
                data["ppi"]=[
                    "value": packet.ppi_data?.hr,
                    "timestamp": packet.ppi_data?.timestamp,
                    "ppiInMs" : packet.ppi_data?.ppiInMs,
                    "ppiErrorEstimate" : packet.ppi_data?.ppiErrorEstimate
                ]
            }
            else {
                data["ppi"]=[
                    "value": 0,
                    "timestamp": 0,
                    "ppiInMs" : 0,
                    "ppiErrorEstimate" : 0
                ]
            }
            
        }
        if(Acc) {
            if(packet.acc_data != nil){
                data["acc"] = packet.acc_data?.compactMap({ accSample in
                    [
                        "x": accSample.x,
                        "y": accSample.y,
                        "z": accSample.z,
                        "timestamp": accSample.timestamp
                    ]
                })}
            else{
                data["acc"] = []
            }
        }
        if(Tmp){
            if(packet.temp_data != nil){
                data["temp"]=[
                    "value": packet.temp_data?.temp,
                    "timestamp": packet.temp_data?.timestamp
                ]
            }
            else{
                data["temp"] = [
                    "value" : 0,
                    "timestamp" : 0
                ]
            }
        }
        

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
                    
                     _  = startOfflineRecordings(identifier: firstAddress)
                    
                   
                }
                else{
                    self.polarConnector.polarApi.disableSDKMode(firstAddress).subscribe(
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
                                if(Hr || Ppi){
                                    self.ppiObservation = self.ppistream(identifier: firstAddress, scheduler: self.schedulerBackground)
                                
                                }
                                if(Acc){
                                    self.accObservation = self.accstream(identifier: firstAddress, scheduler: self.schedulerBackground)
                                }
                                if(Tmp){
                                    self.tmpObservation = self.tmpstream(identifier: firstAddress, scheduler: self.schedulerBackground)
                                }
                            },
                            onError: { error in
                                print("Setup failed with error: \(error)")
                            }
                        )
                        .disposed(by: disposeBag)
                    
                }
            }
            print("returning true")
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
            
            var ppiList: [ppi_data] = []
            var accList: [acc_data] = []
            var tempList: [temp_data] = []
            var hrList: [hr_data] = []
            
            let pack = OfflineDataPack(hr_data:nil, ppi_data:nil, temp_data:nil, acc_data:nil)

            var recordings: [PolarOfflineRecordingEntry] = []
            /*
            self.polarConnector.polarApi.listOfflineRecordings(self.deviceid ?? "")
                .flatMap { recordings -> Observable<PolarOfflineRecordingEntry> in
                    let recordingsArray = Array(arrayLiteral: recordings) // convert to Swift array
                    return Observable.from(recordingsArray)
                }
                .concatMap { recording in
                    // For each recording, fetch the offline data
                    self.polarConnector.polarApi.getOfflineRecord(self.deviceid ?? "", entry: recording, secret: nil)
                        .asObservable()
                        .flatMap { data -> Observable<PolarOfflineRecordingData> in
                            // Process the data and populate lists
                            switch data {
                            case let .accOfflineRecordingData(accData, _, _):
                                accList.append(contentsOf: accData.map { acc_data(x: $0.x, y: $0.y, z: $0.z, timestamp: $0.timeStamp) })
                            case let .ppiOfflineRecordingData(ppiData, _):
                                ppiList.append(contentsOf: ppiData.samples.map { ppi_data(hr: $0.hr, timestamp: $0.timeStamp, ppiInMs: $0.ppInMs, ppiErrorEstimate: $0.ppErrorEstimate) })
                                hrList.append(contentsOf: ppiData.samples.map { hr_data(hr: $0.hr, timestamp: $0.timeStamp) }) // if needed
                            case let .temperatureOfflineRecordingData(tempData, _):
                                tempList.append(contentsOf: tempData.samples.map { temp_data(temp: $0.temperature, Timestamp: $0.timeStamp) })
                            default:
                                print("Not supported")
                            }

                            // After processing, remove the offline record
                            return self.polarConnector.polarApi.removeOfflineRecord(self.deviceid ?? "", entry: recording)
                                .do(
                                    onError: { error in
                                        print("Error deleting record \(recording.path): \(error)")
                                    },
                                        onCompleted: {
                                            print("Record deleted: \(recording.path)")
                                        }
                                        
                                    ).andThen(Observable.just(data)) // continue emitting the processed data
                        }
                }
                .subscribe(
                    onNext: { data in
                        print("Processed data: \(data)")
                    },
                    onError: { error in
                        print("Error processing offline records: \(error)")
                    },
                    onCompleted: {
                        print("All offline recordings processed")
                        print("ACC:", accList.count, "PPI:", ppiList.count, "TEMP:", tempList.count, "HR:", hrList.count)
                        
                        // Here you can store or send your packet
                        pack.acc_data=accList
                        pack.hr_data=hrList
                        pack.ppi_data=ppiList
                        pack.temp_data=tempList
                       
                
                    },
                    
                ).disposed(by: disposeBag ) */
            
            Task(priority: .background) {
                do {
                    // CPU / IO heavy work happens in background
                    let pack = try await processOfflineRecordings()
                    print("pack size")
                    print(pack.acc_data?.count, pack.ppi_data?.count, pack.temp_data?.count)

                    // Hop back to the MainActor for storeData (if it touches CoreData/UI)
                    await MainActor.run {
                        self.storeData(data: pack.toJSON(), timestamp: -1) {
                            print("data stored, sending to backend")
                        }
                    }

                    // Restart offline recordings if needed (background OK)
                    if Continous_recording {
                        _ = self.startOfflineRecordings(identifier: self.deviceid ?? "")
                    }
                    print("before oncomplete")
                    onCompletion()

                } catch {
                    print("Error: \(error)")
                }
            }
            
            
        }
        else{
            self.ppiObservation?.dispose()
            self.accObservation?.dispose()
            self.hrObservation?.dispose()
            deviceListener?.cancel()
            onCompletion()
            
        }
        
        
    }
    
    
    func processOfflineRecordings() async throws -> OfflineDataPack {
        try await withCheckedThrowingContinuation { continuation in
            
            
            var ppiList: [ppi_data] = []
            var accList: [acc_data] = []
            var tempList: [temp_data] = []
            var hrList: [hr_data] = []
            let disposable = self.polarConnector.polarApi
                .listOfflineRecordings(self.deviceid ?? "")
                .flatMap { recordings -> Observable<PolarOfflineRecordingEntry> in
                    let recordingsArray = Array(arrayLiteral: recordings) // convert to Swift array
                    return Observable.from(recordingsArray)
                }
                .concatMap { recording in
                    self.polarConnector.polarApi
                        .getOfflineRecord(self.deviceid ?? "", entry: recording, secret: nil)
                        .asObservable()
                        .flatMap { data -> Observable<PolarOfflineRecordingData> in
                            
                            // Process data (your logic unchanged)
                            switch data {
                            case let .accOfflineRecordingData(accData, _, _):
                                accList.append(contentsOf: accData.map {
                                    acc_data(x: $0.x, y: $0.y, z: $0.z, timestamp: $0.timeStamp)
                                })
                                
                            case let .ppiOfflineRecordingData(ppiData, _):
                                ppiList.append(contentsOf: ppiData.samples.map {
                                    ppi_data(hr: $0.hr, timestamp: $0.timeStamp,
                                             ppiInMs: $0.ppInMs, ppiErrorEstimate: $0.ppErrorEstimate)
                                })
                                hrList.append(contentsOf: ppiData.samples.map {
                                    hr_data(hr: $0.hr, timestamp: $0.timeStamp)
                                })
                                
                            case let .temperatureOfflineRecordingData(tempData, _):
                                tempList.append(contentsOf: tempData.samples.map {
                                    temp_data(temp: $0.temperature, Timestamp: $0.timeStamp)
                                })
                            default:
                                break
                            }
                            
                            return self.polarConnector.polarApi.removeOfflineRecord(self.deviceid ?? "", entry: recording)
                                .do(
                                    onError: { error in
                                        print("Error deleting record \(recording.path): \(error)")
                                    },
                                        onCompleted: {
                                            print("Record deleted: \(recording.path)")
                                        }
                                        
                                    ).andThen(Observable.just(data)) // continue emitting the processed data
                        
                        }
                }
                .subscribe(
                    onNext: { data in
                        print("Processed: \(data)")
                    },
                    onError: { error in
                        continuation.resume(throwing: error)
                    },
                    onCompleted: {
                        print("All recordings processed!")
                        
                        let pack = OfflineDataPack(hr_data:nil, ppi_data:nil, temp_data:nil, acc_data:nil)  // or however you create it
                        pack.acc_data = accList
                        pack.ppi_data = ppiList
                        pack.temp_data = tempList
                        pack.hr_data = hrList
                        
                        continuation.resume(returning: pack)
                    }
                )
            
            // Auto-cancel if the async task is cancelled
            Task {
                await Task.yield()
                if Task.isCancelled {
                    disposable.dispose()
                }
            }
        }
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
                guard let _ = data.first else { return }
               
                let data_formatted: [acc_data] = data.map { sample in
                    acc_data(x: sample.x, y: sample.y, z: sample.z, timestamp: sample.timeStamp)
                }
                self.accQueue.add(data_formatted)
                
                self.tryBuildPacket().map{
                    packet in
                    //dont want to fill up queue with other streams untill hr data starts arriving
                    // so we store state and check for it
                    
                    self.sendOutData(packet: packet)
                }
                    
                
                
               
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
                    if(self.Ppi){
                        self.ppiQueue.add(ppi_data(hr: sample.hr,timestamp: sample.timeStamp,ppiInMs: sample.ppInMs , ppiErrorEstimate: sample.ppErrorEstimate))
                        print("ppiqueadded")
                        self.tryBuildPacket().map {
                            packet in
                            //dont want to fill up queue with other streams untill hr data starts arriving
                            // so we store state and check for it
                            
                            self.sendOutData(packet: packet)
                        }}
                    else{
                        self.hrQueue.add(hr_data(hr:sample.hr,timestamp:sample.timeStamp))
                        print("hrqueadded")
                        self.tryBuildPacket().map {
                            packet in
                            self.sendOutData(packet: packet)
                        }
                    }
                    print("ppi sample \(sample) " )
                }
            },
                       onError:{
                error in
                print("error with ppi stream")
            }
            )
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
                
                self.tempQueue.add(temp_data(temp: sample.temperature, Timestamp: sample.timeStamp))

                self.tryBuildPacket().map {
                    packet in
                    self.sendOutData(packet: packet)
                }
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
    private func DisableSdkMode(identifier: String) -> Disposable {
        return polarConnector.polarApi
            .isSDKModeEnabled(identifier)
            .subscribe(
                onSuccess: { [weak self] sdkEnabled in
                    guard let self = self else { return }

                    if sdkEnabled {
                        _ = self.polarConnector.polarApi
                            .disableSDKMode(identifier)
                            .subscribe(
                                onCompleted: {
                                    print("✅ SDK mode Disable for \(identifier)")
                                },
                                onError: { error in
                                    print("❌ Failed to Disable SDK mode: \(error)")
                                }
                            )
                    } else {
                        print("ℹ️ SDK mode already disabled for \(identifier)")
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
    
    private func startOfflineRecoding(identifier: String,feature:PolarDeviceDataType,settings:PolarSensorSetting?) ->Completable{
        if(settings != nil){
            return polarConnector.polarApi.startOfflineRecording( identifier, feature:feature,settings: settings!,secret: nil)
        }
        if(feature == PolarDeviceDataType.hr || feature == PolarDeviceDataType.ppi){
            return polarConnector.polarApi.startOfflineRecording(identifier, feature: feature, settings: nil, secret: nil)
        }
        else{
            return polarConnector.polarApi.requestOfflineRecordingSettings(identifier, feature: feature)
                    .catch { error in
                        print("Polar360::\(feature) - Settings request failed. Reason: \(error)")
                        let defaultSettings = try PolarSensorSetting( [
                            .sampleRate: 1,
                            .resolution: 1
                        ])
                        return Single.just(defaultSettings)
                    }
                    .observe(on: ConcurrentDispatchQueueScheduler(qos: .background))
                    .flatMapCompletable { settings in
                        print("Polar360::\(feature) - Using settings: \(settings.settings)")
                        return self.polarConnector.polarApi.startOfflineRecording(identifier, feature: feature, settings: settings, secret: nil)
                    }
        }
    }
    
    private func startOfflineRecordings(identifier:String) -> Disposable{
        return self.setupForFirstTimeUse(identifier: identifier).subscribe(
            onCompleted: { [weak self] in
                guard let self else { return }
                print("FTu done ")
                            self.polarConnector.polarApi.disableSDKMode(identifier).subscribe(
                                onCompleted: { [weak self] in
                                    guard let self else { return }
                                    print("sdk mode disabled")
                                    if(self.Acc){
                                        self.startOfflineRecoding(identifier:identifier,feature: .acc , settings:nil ).subscribe(
                                            onCompleted:
                                                {
                                                    print("Acc offline started")
                                                }, onError: {
                                                    error in
                                                    print("error with starting acc offline recording: \(error)")
                                                }
                                        ).disposed(by: self.disposeBag)
                                        
                                    }
                                    if(self.Ppi || self.Hr){
                                        self.startOfflineRecoding(identifier:identifier,feature: .ppi , settings:nil )
                                            .subscribe(
                                                onCompleted: {print("Started Offline Recording for ppi")},
                                                onError: {error in
                                                    print("error when starting offline recording for ppi: \(error)")
                                                },
                                            ).disposed(by: self.disposeBag)
                                    }
                                    if(self.Tmp){
                                        self.startOfflineRecoding(identifier:identifier,feature: .temperature , settings:nil ).subscribe(
                                            onCompleted:
                                                {
                                                    print("temperature offline started")
                                                }, onError: {
                                                    error in
                                                    print("error with starting temperature offline recording: \(error)")
                                                }
                                        ).disposed(by: self.disposeBag)
                                    }

                                },
                                onError: {
                                    error in
                                    print("Error disabling skd mode: \(error)")
                                    
                                    self.polarConnector.polarApi.isSDKModeEnabled(identifier).subscribe(
                                        onSuccess: {
                                            enabled in
                                            if(!enabled){
                                                //we can start the offline recordings
                                                if(self.Acc){
                                                    self.startOfflineRecoding(identifier:identifier,feature: .acc , settings:nil ).subscribe(
                                                        onCompleted:
                                                            {
                                                                print("Acc offline started")
                                                            }, onError: {
                                                                error in
                                                                print("error with starting acc offline recording: \(error)")
                                                            }
                                                    ).disposed(by: self.disposeBag)
                                                    
                                                }
                                                if(self.Ppi || self.Hr){
                                                    self.startOfflineRecoding(identifier:identifier,feature: .ppi , settings:nil )
                                                        .subscribe(
                                                            onCompleted: {print("Started Offline Recording for ppi")},
                                                            onError: {error in
                                                                print("error when starting offline recording for ppi: \(error)")
                                                            },
                                                        ).disposed(by: self.disposeBag)
                                                }
                                                if(self.Tmp){
                                                    self.startOfflineRecoding(identifier:identifier,feature: .temperature , settings:nil ).subscribe(
                                                        onCompleted:
                                                            {
                                                                print("temperature offline started")
                                                            }, onError: {
                                                                error in
                                                                print("error with starting temperature offline recording: \(error)")
                                                            }
                                                    ).disposed(by: self.disposeBag)
                                                }
                                                
                                            }
                                            else{
                                                print("skd mode still enabled cant start offline recording")
                                            }
                                        
                                        },
                                        onFailure: { error in
                                                print("error when checking if skd mode is enabled: \(error)")
                                            }
                                    )
                                    
                                    
                                }
                            ).disposed(by: self.disposeBag)
                        }
                )
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
        if let continuous = settings["continuous_recording"]{
            Continous_recording = String(describing: continuous) == "true"
        }
        
        if let offlineRecording = settings["Offline_recording"] {
            OfflineRecording = String(describing: offlineRecording) == "true"
        }
        if let hrValue = settings["Hr"] {
                Hr = String(describing: hrValue).lowercased() == "true"
            }

        if let accValue = settings["Acc"] {
                Acc = String(describing: accValue).lowercased() == "true"
            }

        if let tempValue = settings["Temp"] {
                Tmp = String(describing: tempValue).lowercased() == "true"
            }

            // MARK: - PPI logic (same as Kotlin)
        if let ppiValue = settings["Ppi"], Hr != true {
                Ppi = String(describing: ppiValue).lowercased() == "true"
            }
        
            print("Hr: \(Hr), Acc: \(Acc), Temp: \(Tmp), Ppi: \(Ppi)")
      
    }
}
