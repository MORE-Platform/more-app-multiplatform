//
//  PolarVerityHeartRateObservation.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 28.03.23.
//  Copyright © 2023 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung 
//  Licensed under the Apache 2.0 license with Commons Clause 
//  (see https://www.apache.org/licenses/LICENSE-2.0 and
//  https://commonsclause.com/).
//

import Foundation
import shared
import PolarBleSdk
import CoreBluetooth
import RxSwift

class PolarVerityHeartRateObservation: Observation_ {
    static var hrReady = false
    
    static func polarDeviceDisconnected() {
        let polarDevices = AppDelegate.shared.mainBluetoothConnector.connected.filter{
            if let deviceName = ($0 as? BluetoothDevice)?.deviceName {
                return deviceName.lowercased().contains("polar")
            }
            return false
        }
        if polarDevices.isEmpty {
            hrReady = false
            AppDelegate.shared.observationManager.pauseObservationType(type: PolarVerityHeartRateType(sensorPermissions: Set()).observationType)
        }
    }
    
    static func setHRReady() {
        if !hrReady {
            hrReady = true
            AppDelegate.shared.observationManager.startObservationType(type: PolarVerityHeartRateType(sensorPermissions: Set()).observationType)
        }
    }
    
    private let deviceIdentificer = "Polar"
    private let polarConnector = AppDelegate.polarConnector
    private let bluetoothRepository = BluetoothDeviceRepository(bluetoothConnector: nil)
    private var connectedDevices: [BluetoothDevice] = []
    private var hrObservation: Disposable? = nil

    init(sensorPermissions: Set<String>) {
        super.init(observationType: PolarVerityHeartRateType(sensorPermissions: sensorPermissions))
        bluetoothRepository.listenForConnectedDevices()
        bluetoothRepository.getConnectedDevices { [weak self] deviceList in
            if let self {
                self.connectedDevices = deviceList.filter{$0.deviceName?.lowercased().contains(self.deviceIdentificer.lowercased()) ?? false}
            }
        }
    }
    
    override func start() -> Bool {
        if self.observerAccessible(){
            if let address = (self.polarConnector.connected.allObjects.first as? BluetoothDevice)?.address {
                hrObservation = self.polarConnector.polarApi.startHrStreaming(address).subscribe(onNext: { [weak self] data in
                    if let self, let hrData = data.first {
                        self.storeData(data: ["hr": hrData.hr], timestamp: -1) {}
                    }
                }, onError: { error in
                    print(error)
                })
                return true
            }
        }
        return false
    }
    
    override func stop(onCompletion: @escaping () -> Void) {
        hrObservation?.dispose()
        onCompletion()
    }
    
    override func observerAccessible() -> Bool {
        if let hasBleObservations = AppDelegate.shared.showBleSetup().second?.boolValue, hasBleObservations {
            if self.polarConnector.connected.count > 0 {
                AppDelegate.shared.coreBluetooth.disableBackgroundScanner()
                return true
            } else {
                AppDelegate.shared.coreBluetooth.enableBackgroundScanner()
                return false
            }
        } else {
            return false
        }
    }
    
    override func applyObservationConfig(settings: Dictionary<String, Any>){
        
    }
    
    override func bleDevicesNeeded() -> Set<String> {
        print("Polar device needed \(Set([deviceIdentificer]))")
        return Set([deviceIdentificer])
    }
    
    override func ableToAutomaticallyStart() -> Bool {
        observerAccessible()
    }
}

