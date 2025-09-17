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
import Combine
import KMPNativeCoroutinesCombine
import Dispatch

class BluetoothConnectionViewModel: ObservableObject {
    private let coreViewModel: CoreBluetoothViewModel = CoreBluetoothViewModel(observationFactory: AppDelegate.shared.observationFactory, coreBluetooth: AppDelegate.shared.bluetoothController)
    private let deviceManager = BluetoothDeviceManager.shared

    @Published var discoveredDevices: [BluetoothDeviceEntity] = []
    @Published var connectedDevices: [BluetoothDeviceEntity] = []
    @Published var connectingDevices: [String] = []

    @Published var bluetoothIsScanning = false

    @Published var neededDevices: [String] = []

    @Published var bluetoothPower: BluetoothState = .off
    
    private var cancellables = Set<AnyCancellable>()

    init() {
        createPublisher(for: deviceManager.connectedDevices)
            .map { devices in
                return Array(devices)
                    .compactMap { device -> BluetoothDeviceEntity? in
                        guard let name = device.deviceName, !name.isEmpty else { return nil }
                        return device
                    }
                    .sorted { ($0.deviceName ?? "") < ($1.deviceName ?? "") }
            }
            .sink(receiveCompletion: { _ in }) { [weak self] devices in
                self?.connectedDevices = devices
            }
            .store(in: &cancellables)
        
        createPublisher(for: deviceManager.devicesCurrentlyConnecting)
            .map { devices in devices.compactMap { $0.address } }
            .removeDuplicates()
            .sink(receiveCompletion: { _ in }) { [weak self] addresses in
                self?.connectingDevices = addresses
            }
            .store(in: &cancellables)
        
        createPublisher(for: deviceManager.discoveredDevices)
            .map { [weak self] devices -> [BluetoothDeviceEntity] in
                let needed = Set(self?.neededDevices ?? [])
                let filtered = devices.compactMap { device -> (BluetoothDeviceEntity, String, Bool)? in
                    guard let name = device.deviceName, !name.isEmpty else { return nil }
                    let matchesNeeded = needed.isEmpty ? false : needed.contains(where: { name.contains($0) })
                    return (device, name, matchesNeeded)
                }
                let sorted = filtered.sorted { lhs, rhs in
                    if lhs.2 != rhs.2 { return lhs.2 && !rhs.2 }
                    return lhs.1 < rhs.1
                }
                return sorted.map { $0.0 }
            }
            .sink(receiveCompletion: { _ in }) { [weak self] devices in
                self?.discoveredDevices = devices
            }
            .store(in: &cancellables)
        
        createPublisher(for: coreViewModel.coreBluetooth.isScanning)
            .removeDuplicates()
            .sink(receiveCompletion: { _ in}) { [weak self] scanning in
                self?.bluetoothIsScanning = scanning.boolValue
            }
            .store(in: &cancellables)
        
        createPublisher(for: coreViewModel.coreBluetooth.bluetoothPower)
            .removeDuplicates()
            .sink(receiveCompletion: { _ in}) { [weak self] power in
                self?.bluetoothPower = power
            }
            .store(in: &cancellables)
    }

    func viewDidAppear() {
        coreViewModel.viewDidAppear()
        neededDevices = Array(AppDelegate.shared.observationFactory.bleDevicesNeeded())
    }

    func viewDidDisappear() {
        coreViewModel.viewDidDisappear()
        ViewManager.shared.showBLEView(state: false)
    }

    func connectToDevice(device: BluetoothDeviceEntity) {
        coreViewModel.connectToDevice(device: device)
    }

    func disconnectFromDevice(device: BluetoothDeviceEntity) {
        coreViewModel.disconnectFromDevice(device: device)
    }
}
