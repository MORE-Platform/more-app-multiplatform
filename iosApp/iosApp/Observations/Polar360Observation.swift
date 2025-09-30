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
    
    static var setstreamstate = false
    
    
    static func setstreamfeature(state: Bool) {
        if state {
            if !setstreamstate {
                AppDelegate.shared.observationManager.startObservationType(type: Polar360Type(sensorPermissions: []).observationType)
            }
        } else {
            Observation_.pauseObservation(PolarVerityHeartRateType(sensorPermissions: []))
        }
        setstreamstate = state
    }
    
    private let deviceIdentificer: Set<String> = ["Polar"]
    private let polarConnector = AppDelegate.polarConnector
    private let disposeBag = DisposeBag( )
    private var connectedDevices: [BluetoothDeviceEntity] = []
    private var hrObservation: Disposable?
    private var tmpObservation: Disposable?
    private var accObservation: Disposable?
    private var ppiObservation: Disposable?

    private let deviceManager = BluetoothDeviceManager.shared

    private var deviceListener: AnyCancellable?
    
    private let errorStringTable = "Errors"
    private var deviceid : String? = nil
    private let schedulerForeground = ConcurrentDispatchQueueScheduler(qos:.userInitiated)
    private let schedulerBackground = ConcurrentDispatchQueueScheduler(qos: .background)
    
    init (repos: MainRepository , sensorPermissions: Set<String>){
        super.init(repos: repos, observationType: Polar360Type(sensorPermissions: sensorPermissions))
    }
    
    
    
        
    override func start() -> Bool {
            if observerAccessible() {
                let acceptableDevices = deviceManager.connectedDevicesValue.deviceWithNameIn(nameSet: deviceIdentificer)
                
                if !acceptableDevices.isEmpty, let firstAddress = acceptableDevices[0].address {
                    deviceid = firstAddress
                    //self.hrObservation = self.hrstream(identifier: firstAddress, scheduler: self.schedulerBackground)
                    
                    
                    setupForFirstTimeUse(identifier: firstAddress)
                        .subscribe(
                            onCompleted: { [weak self] in
                                guard let self else { return }

                                do {
                                    try self.listenToDeviceConnection()
                                } catch {
                                    print("Device connection failed: \(error)")
                                }
                                //this stream does not fire
                                self.hrObservation = self.hrstream(identifier: firstAddress, scheduler: self.schedulerBackground)
                                if self.hrObservation == nil {
                                    print("❌ hrstream returned nil")
                                }

                                self.ppiObservation = self.ppistream(identifier: firstAddress, scheduler: self.schedulerBackground)
                                if self.ppiObservation == nil {
                                    print("❌ ppistream returned nil")
                                }

                                self.accObservation = self.accstream(identifier: firstAddress, scheduler: self.schedulerBackground)
                                if self.accObservation == nil {
                                    print("❌ accstream returned nil")
                                }

                                self.tmpObservation = self.tmpstream(identifier: firstAddress, scheduler: self.schedulerBackground)
                                if self.tmpObservation == nil {
                                    print("❌ tmpstream returned nil")
                                }
                                                     
                            },
                            onError: { error in
                                print("Setup failed with error: \(error)")
                            }
                        )
                        .disposed(by: disposeBag) // ⚡️ Important: make sure you manage subscription lifetime
                }
                return true
            }
            
            print("obsv ended")
            return false
        }
    
    override func stop(onCompletion: @escaping () -> Void) {
        
        self.polarConnector.polarApi.stopOfflineRecording(self.deviceid ?? "" , feature: .acc)
        self.polarConnector.polarApi.stopOfflineRecording(self.deviceid ?? "" , feature: .temperature)
        self.hrObservation?.dispose()
        self.accObservation?.dispose()
        self.hrObservation?.dispose()
        deviceListener?.cancel()
        onCompletion()
    }
    
    private func accstream(identifier:String,scheduler: ConcurrentDispatchQueueScheduler)->Disposable?{
        return polarConnector.polarApi
            .requestStreamSettings(identifier, feature: .acc)
            .catch { error -> Single<PolarSensorSetting> in
                print("ACC settings request failed: \(error)")
                let defaultSettings = try! PolarSensorSetting([
                    .sampleRate: 52,
                    .resolution: 1
                ])
                return Single.just(defaultSettings)
            }
            .asObservable()
            .flatMap { settings in
                print("Using accelerometer settings: \(settings)")
                return self.polarConnector.polarApi.startAccStreaming(identifier, settings: settings)
            }
            .subscribe(on: MainScheduler.instance)
            .observe(on: scheduler)
            .subscribe(
                onNext: { data in
                    guard let sample = data.first else { return }
                    print("x y z: \(sample.x) \(sample.y) \(sample.z), timestamp: \(sample.timeStamp)")
                },
                onError: { error in
                    print("Accelerometer stream failed: \(error)")
                },
                onCompleted: {
                    print("Accelerometer stream finished")
                }
            )
    }
    private func hrstream(identifier:String,scheduler: ConcurrentDispatchQueueScheduler)->Disposable?{
        return polarConnector.polarApi.startHrStreaming(identifier)
            .subscribe(on: scheduler)
            .subscribe(onNext: { [weak self] data in
            if let self, let hrData = data.first {
                self.storeData(data: ["hr": hrData.hr], timestamp: -1) {
                }
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
            .subscribe(onNext: { data in
                if let sample = data.samples.first {
                    print("ppi sample \(sample) asd " )
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
            .observe(on: scheduler)               // process on thread B
            .subscribe(onNext: { data in
                if let sample = data.samples.first {
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
            .andThen(
                Completable.deferred {
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
            .sink(receiveCompletion: {_ in}, receiveValue: { [weak self] devices in
                if let self, !self.deviceIdentificer.anyNameIn(items: devices) {
                    Polar360Observation.setstreamfeature(state: false)
                    self.deviceListener?.cancel()
                }
            })
    }
    override func applyObservationConfig(settings: Dictionary<String, Any>) {
    }
}
