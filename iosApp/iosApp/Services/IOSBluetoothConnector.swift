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
    private var centralManager: CBCentralManager!
    private var discoveredDevices: BluetoothDeviceList = [:]
    private var connectedDevices: BluetoothDeviceList = [:]
    
    var observer: BluetoothConnectorObserver?
    var delegate: BLEConnectorDelegate?

    override init() {
        super.init()
        centralManager = CBCentralManager(delegate: self, queue: nil)
    }

    func connect(device: BluetoothDevice) -> KotlinError? {
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

    func scan() {
        switch centralManager.state {
        case .unknown:
            print("Bluetooth state unknown")
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
            centralManager.scanForPeripherals(withServices: nil, options: [CBCentralManagerScanOptionAllowDuplicatesKey: false])
        @unknown default:
            print("Bluetooth state unknown default")
        }
    }

    func stopScanning() {
        centralManager.stopScan()
    }
    
    deinit {
        stopScanning()
        observer = nil
        delegate = nil
    }
}

extension IOSBluetoothConnector: CBCentralManagerDelegate {
    internal func centralManager(_ central: CBCentralManager, didConnect peripheral: CBPeripheral) {
        print("connected to \(peripheral.description)")
        let device = peripheral.toBluetoothDevice()
        self.connectedDevices[device] = peripheral
        self.observer?.didConnectToDevice(bluetoothDevice: device)
    }
    
    private func centralManager(_ central: CBCentralManager, didDisconnectPeripheral peripheral: CBPeripheral, error: Error?) {
        print("Disconnected from \(peripheral.identifier)")
        let device = peripheral.toBluetoothDevice()
        self.connectedDevices.removeValue(forKey: device)
        self.observer?.didConnectToDevice(bluetoothDevice: device)
    }
    
    private func centralManager(_ central: CBCentralManager, didFailToConnect peripheral: CBPeripheral, error: Error?) {
        self.observer?.didConnectToDevice(bluetoothDevice: peripheral.toBluetoothDevice())
    }
    
    internal func centralManagerDidUpdateState(_ central: CBCentralManager) {
        print("Manager state is powered on: \(central.state == .poweredOn)")
        if central.state == .poweredOn {
            delegate?.bleHasPower()
        }
    }
    
    internal func centralManager(_ central: CBCentralManager, didDiscover peripheral: CBPeripheral, advertisementData: [String: Any], rssi RSSI: NSNumber) {
        if peripheral.name != nil {
            let device = peripheral.toBluetoothDevice()
            discoveredDevices[device] = peripheral
        }
    }
}

extension IOSBluetoothConnector: CBPeripheralDelegate {
}

extension CBPeripheral {
    func toBluetoothDevice() -> BluetoothDevice {
        BluetoothDevice(
            deviceId: self.identifier.uuidString,
            deviceName: self.name ?? "Unknown",
            address: self.identifier.uuidString,
            isConnectable: true
        )
    }
}
