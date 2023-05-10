//
//  PolarVerityHeartRateObservation.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 28.03.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import Foundation
import shared
import PolarBleSdk
import CoreBluetooth
import RxSwift

class PolarVerityHeartRateObservation: Observation_ {
    static var hrReady = false
    
    private let polarConnector = AppDelegate.polarConnector
    private let bluetoothRepository = BluetoothDeviceRepository(bluetoothConnector: nil)
    private var connectedDevices: [BluetoothDevice] = []
    private var hrObservation: Disposable? = nil

    init(sensorPermissions: Set<String>) {
        super.init(observationType: PolarVerityHeartRateType(sensorPermissions: sensorPermissions))
        bluetoothRepository.listenForConnectedDevices()
        bluetoothRepository.getConnectedDevices { [weak self] deviceList in
            if let self {
                self.connectedDevices = deviceList.filter{$0.deviceName?.lowercased().contains("polar") ?? false}
            }
        }
    }
    
    override func start() -> Bool {
        if !self.connectedDevices.isEmpty {
            if let address = connectedDevices.first?.address {
                hrObservation = polarConnector.polarApi.startHrStreaming(address).subscribe(onNext: { [weak self] data in
                    if let self, let hrData = data.first {
                        print("New HR data: \(hrData.hr)")
                        self.storeData(data: ["hr": hrData.hr], timestamp: -1){
                            
                        }
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
        !self.connectedDevices.isEmpty
    }
    
    override func applyObservationConfig(settings: Dictionary<String, Any>){
        
    }
}

