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
    private let coreViewModel: CoreBluetoothConnectionViewModel
    
    @Published var discoveredDevices: [BluetoothDevice] = []
    @Published var connectedDevices: [BluetoothDevice] = []
    @Published var connectingDevices: [String] = []
    
    @Published var bluetoothIsScanning = false
    
    init(bluetoothConnector: BluetoothConnector) {
        self.coreViewModel = CoreBluetoothConnectionViewModel(bluetoothConnector: bluetoothConnector, scanDuration: 10000, scanInterval: 5000)
        
        self.coreViewModel.discoveredDevicesListChanges { [weak self] deviceSet in
            if let self {
                DispatchQueue.main.async {
                    self.discoveredDevices = Array(deviceSet).filter{ $0.deviceName != nil}
                }
            }
        }
        
        self.coreViewModel.connectedDevicesListChanges { [weak self] deviceSet in
            if let self {
                DispatchQueue.main.async {
                    self.connectedDevices = Array(deviceSet).filter{ $0.deviceName != nil}
                }
            }
        }
        
        self.coreViewModel.scanningIsChanging { [weak self] scanning in
            DispatchQueue.main.async {
                self?.bluetoothIsScanning = scanning.boolValue
            }
        }
        
        self.coreViewModel.connectingDevicesListChanges { [weak self] connectingDevices in
            DispatchQueue.main.async {
                self?.connectingDevices = Array(connectingDevices)
            }
        }
    }
    
    func viewDidAppear() {
        coreViewModel.viewDidAppear()
    }
    
    func viewDidDisappear() {
        coreViewModel.viewDidDisappear()
        discoveredDevices.removeAll()
    }
    
    func scanningForDevices() {
        coreViewModel.scanForDevices()
    }
    
    func stopScanning() {
        coreViewModel.stopScanning()
    }
    
    func connectToDevice(device: BluetoothDevice) {
        coreViewModel.connectToDevice(device: device)
    }
    
    func disconnectFromDevice(device: BluetoothDevice) {
        coreViewModel.disconnectFromDevice(device: device)
    }
}
