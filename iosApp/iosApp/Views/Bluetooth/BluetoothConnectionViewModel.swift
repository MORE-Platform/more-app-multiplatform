//
//  BluetoothConnectionViewModel.swift
//  More
//
//  Created by Jan Cortiel on 24.04.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import Foundation
import shared

class BluetoothConnectionViewModel: ObservableObject {
    private let coreViewModel: CoreBLESetupViewModel = CoreBLESetupViewModel(observationFactory: AppDelegate.shared.observationFactory, bluetoothConnector: AppDelegate.shared.mainBluetoothConnector)
    
    @Published var discoveredDevices: [BluetoothDevice] = []
    @Published var connectedDevices: [BluetoothDevice] = []
    @Published var connectingDevices: [String] = []
    
    @Published var bluetoothIsScanning = false
    
    @Published var neededDevices: [String] = []
    
    init() {
        self.coreViewModel.coreBluetooth.discoveredDevicesListChanges { [weak self] deviceSet in
            if let self {
                DispatchQueue.main.async {
                    self.discoveredDevices = Array(deviceSet).filter{ $0.deviceName != nil}
                }
            }
        }
        
        self.coreViewModel.coreBluetooth.connectedDevicesListChanges { [weak self] deviceSet in
            if let self {
                DispatchQueue.main.async {
                    self.connectedDevices = Array(deviceSet).filter{ $0.deviceName != nil}
                }
            }
        }
        
        self.coreViewModel.coreBluetooth.scanningIsChanging { [weak self] scanning in
            DispatchQueue.main.async {
                self?.bluetoothIsScanning = scanning.boolValue
            }
        }
        
        self.coreViewModel.coreBluetooth.connectingDevicesListChanges { [weak self] connectingDevices in
            DispatchQueue.main.async {
                self?.connectingDevices = Array(connectingDevices)
            }
        }
        
        self.coreViewModel.devicesNeededChange { [weak self] deviceList in
            DispatchQueue.main.async {
                self?.neededDevices = Array(deviceList)
            }
        }
    }
    
    func viewDidAppear() {
        coreViewModel.viewDidAppear()
    }
    
    func viewDidDisappear() {
        coreViewModel.viewDidDisappear()
    }
    
    func connectToDevice(device: BluetoothDevice) {
        coreViewModel.connectToDevice(device: device)
    }
    
    func disconnectFromDevice(device: BluetoothDevice) {
        coreViewModel.disconnectFromDevice(device: device)
    }
}
