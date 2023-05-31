//
//  IOSBluetoothConnector.swift
//  More
//
//  Created by Jan Cortiel on 19.04.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import CoreBluetooth
import Foundation
import PolarBleSdk
import RxSwift
import shared

protocol BLEConnectorDelegate {
    func bleHasPower()
}

typealias BluetoothDeviceList = [BluetoothDevice: CBPeripheral]

class IOSBluetoothConnector: NSObject, BluetoothConnector {
    var connected: KotlinMutableSet<BluetoothDevice> = KotlinMutableSet()
    var discovered: KotlinMutableSet<BluetoothDevice> = KotlinMutableSet()
    
    internal let specificBluetoothConnectors: [String : BluetoothConnector] = ["polar": AppDelegate.polarConnector]
    
    private var centralManager: CBCentralManager!
    private var discoveredDevices: BluetoothDeviceList = [:]
    private var connectedDevices: BluetoothDeviceList = [:]
    private var scanning = false
    
    weak var observer: BluetoothConnectorObserver?
    var delegate: BLEConnectorDelegate?
    
    private var scanningWithUnknownBLEState = false
    
    override init() {
        super.init()
        centralManager = CBCentralManager(delegate: self, queue: nil)
        specificBluetoothConnectors.values.forEach{ $0.observer = self }
    }

    func connect(device: BluetoothDevice) -> KotlinError? {
        print("Connecting to device: \(device)")
        let (hasConnected, error) = connectToSpecificDevice(device: device)
        if (hasConnected) {
            guard let error else {
                return nil
            }
            print(error)
            return error
        }
        if let cbPeripheral = discoveredDevices[device] {
            centralManager.connect(cbPeripheral)
            return nil
        } else {
            return KotlinError(message: "Could not find device")
        }
    }
    
    func disconnect(device: BluetoothDevice) {
        if let cbPeripheral = discoveredDevices[device] {
            centralManager.cancelPeripheralConnection(cbPeripheral)
        }
    }
    
    func isScanning() -> Bool {
        scanning
    }
    
    
    func scan() {
        if !scanning {
            switch centralManager.state {
            case .unknown:
                print("Bluetooth state unknown")
                scanningWithUnknownBLEState = true
            case .resetting:
                print("Bluetooth state resetting")
            case .unsupported:
                print("Bluetooth state unsupported")
            case .unauthorized:
                print("Bluetooth state unauthorized")
            case .poweredOff:
                print("Bluetooth state powered off")
            case .poweredOn:
                print("Bluetooth state powered on")
                scanning = true
                centralManager.scanForPeripherals(withServices: nil, options: [CBCentralManagerScanOptionAllowDuplicatesKey: false])
            @unknown default:
                print("Bluetooth state unknown default")
            }
        }
    }
    
    func stopScanning() {
        scanning = false
        centralManager.stopScan()
    }
    
    func isConnectingToDevice(bluetoothDevice: BluetoothDevice) {
        observer?.isConnectingToDevice(bluetoothDevice: bluetoothDevice)
    }
    
    func didConnectToDevice(bluetoothDevice: BluetoothDevice) {
        observer?.didConnectToDevice(bluetoothDevice: bluetoothDevice)
    }
    
    func didDisconnectFromDevice(bluetoothDevice: BluetoothDevice) {
        observer?.didDisconnectFromDevice(bluetoothDevice: bluetoothDevice)
    }
    
    func didFailToConnectToDevice(bluetoothDevice: BluetoothDevice) {
        observer?.didFailToConnectToDevice(bluetoothDevice: bluetoothDevice)
    }
    
    func discoveredDevice(device: BluetoothDevice) {
        observer?.discoveredDevice(device: device)
    }
    
    func removeDiscoveredDevice(device: BluetoothDevice) {
        observer?.removeDiscoveredDevice(device: device)
    }
    
    private func connectToSpecificDevice(device: BluetoothDevice) -> (Bool, KotlinError?) {
        if let connector = specificBluetoothConnectors.first(where: {device.deviceName?.lowercased().contains($0.key.lowercased()) ?? false}) {
            return (true, connector.value.connect(device: device))
        }
        return (false, nil)
    }
    
    private func disconnectFromSpecificDevice(device: BluetoothDevice) -> Bool {
        if let connector = specificBluetoothConnectors.first(where: {device.deviceName?.lowercased().contains($0.key.lowercased()) ?? false}) {
            connector.value.disconnect(device: device)
            return true
        }
        return false
    }
    
    func close() {
        stopScanning()
    }
    
    deinit {
        stopScanning()
    }
}

extension IOSBluetoothConnector: CBCentralManagerDelegate {
    func centralManager(_ central: CBCentralManager, didConnect peripheral: CBPeripheral) {
        print("Connected to \(peripheral.description)")
        let device = peripheral.toBluetoothDevice()
        self.connectedDevices[device] = peripheral
        self.observer?.didConnectToDevice(bluetoothDevice: device)
    }
    
    func centralManager(_ central: CBCentralManager, didDisconnectPeripheral peripheral: CBPeripheral) {
        print("Disconnected from \(peripheral.identifier)")
        let device = peripheral.toBluetoothDevice()
        self.connectedDevices.removeValue(forKey: device)
        self.observer?.didConnectToDevice(bluetoothDevice: device)
    }
    
    func centralManager(_ central: CBCentralManager, didFailToConnect peripheral: CBPeripheral) {
        print("Did fail to connect to device: \(peripheral.identifier)")
        self.observer?.didFailToConnectToDevice(bluetoothDevice: peripheral.toBluetoothDevice())
    }
    
    func centralManagerDidUpdateState(_ central: CBCentralManager) {
        print("Manager state is powered on: \(central.state == .poweredOn)")
        if central.state == .poweredOn {
            delegate?.bleHasPower()
            if scanningWithUnknownBLEState {
                self.scan()
            }
        }
        if central.state != .unknown {
            scanningWithUnknownBLEState = false
        }
    }
    
    internal func centralManager(_ central: CBCentralManager, didDiscover peripheral: CBPeripheral, advertisementData: [String: Any], rssi RSSI: NSNumber) {
        if peripheral.name != nil {
            let device = peripheral.toBluetoothDevice()
            if peripheral.state == .connected {
                device.connected = true
                observer?.didConnectToDevice(bluetoothDevice: device)
                connectedDevices[device] = peripheral
            } else {
                observer?.discoveredDevice(device: device)
                discoveredDevices[device] = peripheral
            }
        }
    }
}

extension IOSBluetoothConnector: CBPeripheralDelegate {
    
}

extension CBPeripheral {
    func toBluetoothDevice() -> BluetoothDevice {
        BluetoothDevice.Companion().create(
            deviceId: self.identifier.uuidString,
            deviceName: self.name ?? "Unknown",
            address: self.identifier.uuidString,
            isConnectable: true
        )
    }
}
