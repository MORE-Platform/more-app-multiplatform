//
//  PolarVerityHeartRateObservation.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 28.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import Foundation
import shared
import PolarBleSdk
import CoreBluetooth
import RxSwift

class PolarVerityHeartRateObservation: Observation_ {
    
    private let api = PolarBleApiDefaultImpl.polarImplementation(DispatchQueue.main, features: [PolarBleSdkFeature.feature_hr])
    private var deviceId = ""
    private var connected = false
    private var devicesSubscription: Disposable? = nil
    private var hrObservation: Disposable? = nil
    
    init(sensorPermissions: Set<String>) {
        super.init(observationTypeImpl: PolarVerityHeartRateType(sensorPermissions: sensorPermissions))
        searchDevices()
    }
    
    override func start() -> Bool {
        if !self.connected {
            self.searchDevices()
        }
        if self.connected {
            self.hrFeatureReady(deviceId)
            return true
        }
        return false
    }
    
    override func stop() {
        hrObservation?.dispose()
    }
    
    override func observerAccessible() -> Bool {
        self.connected
    }
    
    override func applyObservationConfig(settings: Dictionary<String, Any>){
        
    }
    
    private func searchDevices() {
        self.devicesSubscription = api.searchForDevice().subscribe(onNext: { device in
            self.deviceId = device.deviceId
            if !self.connected {
                self.connectDevice(deviceId: device.deviceId)
            }
        })
    }
    
    private func connectDevice(deviceId: String) {
        do {
            try api.connectToDevice(deviceId)
            self.connected = true
            self.devicesSubscription?.dispose()
        } catch {
            print("Error: Could not connect to device")
        }
    }
    
    private func disconnectDevice(deviceId: String) {
        do {
            try api.disconnectFromDevice(deviceId)
            self.connected = false
        } catch {
            print("Error: Could not disconnect")
        }
    }
}

extension PolarVerityHeartRateObservation: PolarBleApiObserver {
    func deviceConnecting(_ identifier: PolarBleSdk.PolarDeviceInfo) {
        
    }
    
    func deviceConnected(_ identifier: PolarBleSdk.PolarDeviceInfo) {
        
    }
    
    func deviceDisconnected(_ identifier: PolarBleSdk.PolarDeviceInfo) {
        
    }
}

extension PolarVerityHeartRateObservation: PolarBleApiPowerStateObserver {
    func blePowerOn() {
        
    }
    
    func blePowerOff() {
        
    }
}

extension PolarVerityHeartRateObservation: PolarBleApiDeviceInfoObserver {
    func batteryLevelReceived(_ identifier: String, batteryLevel: UInt) {
        
    }
    
    func disInformationReceived(_ identifier: String, uuid: CBUUID, value: String) {
        
    }
}

extension PolarVerityHeartRateObservation: PolarBleApiDeviceFeaturesObserver {
    func hrFeatureReady(_ identifier: String) {
        print("feature ready")
        self.hrObservation = self.api.startHrStreaming(self.deviceId).subscribe { value in
            if let element = value.element {
                self.storeData(data: ["hr": element[0].hr])
            }
        }
    }
    
    func ftpFeatureReady(_ identifier: String) {
        
    }
    
    func streamingFeaturesReady(_ identifier: String, streamingFeatures: Set<PolarBleSdk.PolarDeviceDataType>) {
        
    }
    
    func bleSdkFeatureReady(_ identifier: String, feature: PolarBleSdk.PolarBleSdkFeature) {
        
    }
}

extension PolarVerityHeartRateObservation: PolarBleApiDeviceHrObserver {
    func hrValueReceived(_ identifier: String, data: (hr: UInt8, rrs: [Int], rrsMs: [Int], contact: Bool, contactSupported: Bool)) {
        
    }
}

