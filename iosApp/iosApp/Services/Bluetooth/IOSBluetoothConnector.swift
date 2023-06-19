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
    let specificBluetoothConnectors: KotlinMutableDictionary<NSString, BluetoothConnector> = KotlinMutableDictionary<NSString, BluetoothConnector>()
    
    let connected: KotlinMutableSet<BluetoothDevice> = KotlinMutableSet()
    let discovered: KotlinMutableSet<BluetoothDevice> = KotlinMutableSet()
    
    private var centralManager: CBCentralManager!
    private var discoveredDevices: BluetoothDeviceList = [:]
    private var connectedDevices: BluetoothDeviceList = [:]
    var scanning = false
    var bluetoothState: BluetoothState = .off
    
    weak var observer: BluetoothConnectorObserver?
    var delegate: BLEConnectorDelegate?
    
    private var scanningWithUnknownBLEState = false
    
    override init() {
        super.init()
        centralManager = CBCentralManager(delegate: self, queue: nil)
        specificBluetoothConnectors.allValues.forEach{ ($0 as? BluetoothConnector)?.observer = self }
    }
    
    func addSpecificBluetoothConnector(key: String, connector: BluetoothConnector) {
        specificBluetoothConnectors[key] = connector
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
    
    func applyObserver(bluetoothConnectorObserver: BluetoothConnectorObserver?) {
        observer = bluetoothConnectorObserver
        replayStates()
    }
    
    func replayStates() {
        self.connectedDevices.keys.forEach{didConnectToDevice(bluetoothDevice: $0)}
        self.discoveredDevices.keys.forEach{ didDiscoverDevice(device: $0)}
        isScanning(boolean: scanning)
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
    
    func didDiscoverDevice(device: BluetoothDevice) {
        observer?.didDiscoverDevice(device: device)
    }
    
    func removeDiscoveredDevice(device: BluetoothDevice) {
        observer?.removeDiscoveredDevice(device: device)
    }
    
    func onBluetoothStateChange(bluetoothState: BluetoothState) {
        self.bluetoothState = bluetoothState
        observer?.onBluetoothStateChange(bluetoothState: bluetoothState)
    }
    
    private func connectToSpecificDevice(device: BluetoothDevice) -> (Bool, KotlinError?) {
        if let connector = specificBluetoothConnectors
            .first(where: {device.deviceName?.lowercased().contains(($0.key as? String)?.lowercased() ?? "") ?? false})?.value as? BluetoothConnector {
            return (true, connector.connect(device: device))
        }
        return (false, nil)
    }
    
    private func disconnectFromSpecificDevice(device: BluetoothDevice) -> Bool {
        if let connector = specificBluetoothConnectors
            .first(where: {device.deviceName?.lowercased().contains(($0.key as? String)?.lowercased() ?? "") ?? false})?.value as? BluetoothConnector {
            connector.disconnect(device: device)
            return true
        }
        return false
    }
    
    func isScanning(boolean: Bool) {
        scanning = boolean
        observer?.isScanning(boolean: boolean)
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
        self.connectedDevices.removeValue(forKey: peripheral.toBluetoothDevice())
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
                connectedDevices[device] = peripheral
                observer?.didConnectToDevice(bluetoothDevice: device)
            } else {
                discoveredDevices[device] = peripheral
                observer?.didDiscoverDevice(device: device)
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
