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
        coreViewModel.coreBluetooth.discoveredDevicesListChanges { [weak self] deviceSet in
            if let self {
                DispatchQueue.main.async {
                    self.discoveredDevices = Array(deviceSet)
                        .filter { $0.deviceName != nil && !($0.deviceName?.isEmpty ?? true) }
                        .sorted(by: { d1, d2 in
                            if let name1 = d1.deviceName, let name2 = d2.deviceName {
                                let name1ContainsKeyword = self.neededDevices.contains(where: name1.contains)
                                let name2ContainsKeyword = self.neededDevices.contains(where: name2.contains)
                                if name1ContainsKeyword && !name2ContainsKeyword {
                                    return true
                                } else if !name1ContainsKeyword && name2ContainsKeyword {
                                    return false
                                } else {
                                    return name1 < name2
                                }
                            } else {
                                return false
                            }
                        })
                }
            }
        }

        coreViewModel.coreBluetooth.connectedDevicesListChanges { [weak self] deviceSet in
            if let self {
                DispatchQueue.main.async {
                    self.connectedDevices = Array(deviceSet)
                        .filter { $0.deviceName != nil && !($0.deviceName?.isEmpty ?? true) }
                        .sorted(by: { d1, d2 in
                            if let name1 = d1.deviceName, let name2 = d2.deviceName {
                                return name1 < name2
                            } else {
                                return false
                            }
                        })
                }
            }
        }

        coreViewModel.coreBluetooth.scanningIsChanging { [weak self] scanning in
            DispatchQueue.main.async {
                self?.bluetoothIsScanning = scanning.boolValue
            }
        }

        coreViewModel.coreBluetooth.connectingDevicesListChanges { [weak self] connectingDevices in
            DispatchQueue.main.async {
                self?.connectingDevices = Array(connectingDevices)
            }
        }

        coreViewModel.devicesNeededChange { [weak self] deviceList in
            DispatchQueue.main.async {
                self?.neededDevices = Array(AppDelegate.shared.observationFactory.bleDevicesNeeded(types: deviceList))
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
