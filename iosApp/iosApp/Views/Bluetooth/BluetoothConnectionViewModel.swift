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
    
    @Published var bluetoothIsScanning = false
    
    init(bluetoothConnector: BluetoothConnector) {
        self.coreViewModel = CoreBluetoothConnectionViewModel(bluetoothConnector: bluetoothConnector)
        
        self.coreViewModel.discoveredDevicesListChanges { [weak self] deviceSet in
            if let self {
                self.discoveredDevices = Array(deviceSet).filter{ $0.deviceName != nil}
            }
        }
        
        self.coreViewModel.connectedDevicesListChanges { [weak self] deviceSet in
            if let self {
                self.connectedDevices = Array(deviceSet).filter{ $0.deviceName != nil}
            }
        }
        
        self.coreViewModel.scanningIsChanging { [weak self] in self?.bluetoothIsScanning = $0.boolValue}
    }
    
    func viewDodAppear() {
        coreViewModel.viewDidAppear()
    }
    
    func viewDidDisappear() {
        coreViewModel.viewDidDisappear()
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
