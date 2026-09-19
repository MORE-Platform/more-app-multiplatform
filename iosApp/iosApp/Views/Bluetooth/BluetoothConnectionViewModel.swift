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
//  Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
//

import Combine
import Dispatch
import Foundation
import KMPNativeCoroutinesCombine
import shared

class BluetoothConnectionViewModel: ObservableObject {
    private let coreViewModel: CoreBluetoothViewModel = CoreBluetoothViewModel(observationFactory: AppDelegate.shared.observationFactory, coreBluetooth: AppDelegate.shared.bluetoothController)
    private let bleManager = BluetoothStateManagement.shared

    @Published var discoveredDevices: [BluetoothDeviceEntity] = []
    @Published var connectedDevices: [BluetoothDeviceEntity] = []
    @Published var connectingDevices: [String] = []

    @Published var bluetoothIsScanning = false

    @Published var neededDevices: [String] = []

    @Published var bluetoothPower = false

    private var cancellables = Set<AnyCancellable>()

    init() {
        createPublisher(for: bleManager.connectedDevices)
        .map { devices in
            Array(devices)
            .compactMap { device -> BluetoothDeviceEntity? in
                guard let name = device.deviceName, !name.isEmpty else {
                    return nil
                }
                return device
            }
            .sorted {
                ($0.deviceName ?? "") < ($1.deviceName ?? "")
            }
        }
        .receive(on: DispatchQueue.main)
        .sink(receiveCompletion: { _ in }) { [weak self] devices in
            self?.connectedDevices = devices
        }
        .store(in: &cancellables)

        createPublisher(for: bleManager.devicesCurrentlyConnecting)
        .map { devices in
            devices.compactMap {
                $0.address
            }
        }
        .removeDuplicates()
        .receive(on: DispatchQueue.main)
        .sink(receiveCompletion: { _ in }) { [weak self] addresses in
            self?.connectingDevices = addresses
        }
        .store(in: &cancellables)

        createPublisher(for: bleManager.discoveredDevices)
        .map { [weak self] devices -> [BluetoothDeviceEntity] in
            let needed = Set(self?.neededDevices ?? [])
            let filtered = devices.compactMap { device -> (BluetoothDeviceEntity, String, Bool)? in
                guard let name = device.deviceName, !name.isEmpty else {
                    return nil
                }
                let matchesNeeded = needed.isEmpty ? false : needed.contains(where: { name.contains($0) })
                return (device, name, matchesNeeded)
            }
            let sorted = filtered.sorted { lhs, rhs in
                if lhs.2 != rhs.2 {
                    return lhs.2 && !rhs.2
                }
                return lhs.1 < rhs.1
            }
            return sorted.map {
                $0.0
            }
        }
        .receive(on: DispatchQueue.main)
        .sink(receiveCompletion: { _ in }) { [weak self] devices in
            self?.discoveredDevices = devices
        }
        .store(in: &cancellables)

        createPublisher(for: bleManager.scanning)
        .removeDuplicates()
        .receive(on: DispatchQueue.main)
        .sink(receiveCompletion: { _ in }) { [weak self] scanning in
            self?.bluetoothIsScanning = scanning.boolValue
        }
        .store(in: &cancellables)

        createPublisher(for: bleManager.bluetoothActive)
        .removeDuplicates()
        .receive(on: DispatchQueue.main)
        .sink(receiveCompletion: { _ in }) { [weak self] power in
            self?.bluetoothPower = power.boolValue
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
        Task {
            do {
                try await coreViewModel.connectToDevice(device: device)
            } catch {
                print("Cannot connect to bluetooth device: \(device.deviceName ?? "Unknown"): \(error)")
            }
        }
    }

    func disconnectFromDevice(device: BluetoothDeviceEntity) {
        coreViewModel.disconnectFromDevice(device: device)
    }
}
