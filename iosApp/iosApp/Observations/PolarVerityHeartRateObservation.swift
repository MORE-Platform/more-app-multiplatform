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
    
    init(sensorPermissions: Set<String>) {
        super.init(observationType: PolarVerityHeartRateType(sensorPermissions: sensorPermissions))
        searchDevices()
    }
    
    override func start() -> Bool {
        return true
    }
    
    override func stop(onCompletion: @escaping () -> Void) {
        onCompletion()
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
                print(self.deviceId)
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

