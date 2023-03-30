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

class PolarVerityHeartRateObservation: Observation_,
                                       PolarBleApiObserver,
                                       PolarBleApiPowerStateObserver,
                                       PolarBleApiDeviceInfoObserver,
                                       PolarBleApiDeviceFeaturesObserver,
                                       PolarBleApiDeviceHrObserver {
    
    var api = PolarBleApiDefaultImpl.polarImplementation(DispatchQueue.main, features: [PolarBleSdkFeature.feature_hr])
    var deviceId = ""
    var connected = false
    var devicesSubscription: Disposable? = nil
    
    init(sensorPermissions: Set<String>) {
        super.init(observationTypeImpl: PolarVerityHeartRateType(sensorPermissions: sensorPermissions))
        self.getDeviceId()
    }
    
    func getDeviceId() {
        self.devicesSubscription = api.searchForDevice().subscribe(onNext: { device in
            self.deviceId = device.deviceId
            if !self.connected {
                self.connectDevice(deviceId: device.deviceId)
            } else {
                self.disconnectDevice(deviceId: device.deviceId)
            }
        })
    }
    
    func connectDevice(deviceId: String) {
        do {
            try api.connectToDevice(deviceId)
            self.connected = true
            self.devicesSubscription?.dispose()
        } catch {
            print("Error: Could not connect to device")
        }
    }
    
    func disconnectDevice(deviceId: String) {
        do {
            try api.disconnectFromDevice(deviceId)
            self.connected = false
        } catch {
            print("Error: Could not disconnect")
        }
    }
    
    func deviceConnecting(_ identifier: PolarBleSdk.PolarDeviceInfo) {
        print("try connecting to: \(identifier.name)")
    }
    
    func deviceConnected(_ identifier: PolarBleSdk.PolarDeviceInfo) {
        print("device connected: \(identifier.name)")
    }
    
    func deviceDisconnected(_ identifier: PolarBleSdk.PolarDeviceInfo) {
        
    }
    
    func blePowerOn() {
        
    }
    
    func blePowerOff() {
        
    }
    
    func batteryLevelReceived(_ identifier: String, batteryLevel: UInt) {
        
    }
    
    func disInformationReceived(_ identifier: String, uuid: CBUUID, value: String) {
        
    }
    
    func hrFeatureReady(_ identifier: String) {
        
    }
    
    func ftpFeatureReady(_ identifier: String) {
        
    }
    
    func streamingFeaturesReady(_ identifier: String, streamingFeatures: Set<PolarBleSdk.PolarDeviceDataType>) {
        
    }
    
    func bleSdkFeatureReady(_ identifier: String, feature: PolarBleSdk.PolarBleSdkFeature) {
        
    }
    
    func hrValueReceived(_ identifier: String, data: (hr: UInt8, rrs: [Int], rrsMs: [Int], contact: Bool, contactSupported: Bool)) {
        
    }
}
