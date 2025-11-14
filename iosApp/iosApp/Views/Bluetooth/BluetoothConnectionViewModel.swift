//
//  BluetoothConnectionViewModel.swift
//  More
//
//  Created by Jan Cortiel on 24.04.23.
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

class BluetoothConnectionViewModel: ObservableObject {
    private let coreViewModel: CoreBluetoothViewModel = CoreBluetoothViewModel(observationFactory: AppDelegate.shared.observationFactory, coreBluetooth: AppDelegate.shared.bluetoothController)
    private let deviceManager = BluetoothDeviceManager.shared

    @Published var discoveredDevices: [BluetoothDevice] = []
    @Published var connectedDevices: [BluetoothDevice] = []
    @Published var connectingDevices: [String] = []

    @Published var bluetoothIsScanning = false

    @Published var neededDevices: [String] = []

    @Published var bluetoothPower: BluetoothState = .off

    init() {
        deviceManager.connectedDevicesAsClosure { [weak self] deviceSet in
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

        deviceManager.devicesCurrentlyConnectingAsClosure { [weak self] devices in
            if let self {
                DispatchQueue.main.async {
                    self.connectingDevices = devices.compactMap { $0.address }
                }
            }
        }

        deviceManager.discoveredDevicesAsClosure { [weak self] deviceSet in
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

        coreViewModel.coreBluetooth.isScanningAsClosure { [weak self] kBool in
            self?.bluetoothIsScanning = kBool.boolValue
        }

        coreViewModel.coreBluetooth.bluetoothStateAsClosure { [weak self] bluetoothState in
            self?.bluetoothPower = bluetoothState
        }
    }

    func viewDidAppear() {
        coreViewModel.viewDidAppear()
        neededDevices = Array(AppDelegate.shared.observationFactory.bleDevicesNeeded())
    }

    func viewDidDisappear() {
        coreViewModel.viewDidDisappear()
        ViewManager.shared.showBLEView(state: false)
    }

    func connectToDevice(device: BluetoothDevice) {
        coreViewModel.connectToDevice(device: device)
    }

    func disconnectFromDevice(device: BluetoothDevice) {
        coreViewModel.disconnectFromDevice(device: device)
    }
}
