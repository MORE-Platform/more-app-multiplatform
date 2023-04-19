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
    
    var delegate: BLEConnectorDelegate?

    override init() {
        super.init()
        centralManager = CBCentralManager(delegate: self, queue: nil)
    }

    func connect(device: BluetoothDevice) {
        if let cbPeripheral = discoveredDevices[device] {
            centralManager.connect(cbPeripheral)
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


//    var deviceList: [BluetoothDevice] = []
//    private let polarApi = PolarBleApiDefaultImpl
//        .polarImplementation(DispatchQueue.main, features: [
//            .feature_hr,
//            .feature_battery_info,
//            .feature_device_info,
//            .feature_polar_offline_recording,
//            .feature_polar_online_streaming,
//            .feature_polar_sdk_mode
//        ])
//    private var disposable: Disposable?
//
//    func connect(device: BluetoothDevice) {
//        do {
//            try self.polarApi.connectToDevice(device.deviceId)
//        } catch {
//            print(error)
//        }
//    }
//
//    func disconnect(device: BluetoothDevice) {
//        do {
//            try self.polarApi.disconnectFromDevice(device.deviceId)
//        } catch {
//            print(error)
//        }
//    }
//
//    func scan() {
//        self.disposable = polarApi.searchForDevice().subscribe(onNext: { [weak self] deviceInfo in
//            if let self {
//                let bluetoothDevice = BluetoothDevice(deviceId: deviceInfo.deviceId, deviceName: deviceInfo.name, address: deviceInfo.address.uuidString, isConnectable: deviceInfo.connectable)
//                self.deviceList.append(bluetoothDevice)
//            }
//        }, onError: { [weak self] err in
//            print(err)
//            self?.disposable?.dispose()
//        })
//    }
//
//    func stopScanning() {
//        self.disposable?.dispose()
//    }
}
//
extension IOSBluetoothConnector: CBCentralManagerDelegate {
    internal func centralManager(_ central: CBCentralManager, didConnect peripheral: CBPeripheral) {
        print("connected to \(peripheral.description)")
        self.connectedDevices[peripheral.toBluetoothDevice()] = peripheral
    }
    
    private func centralManager(_ central: CBCentralManager, didDisconnectPeripheral peripheral: CBPeripheral, error: Error?) {
        print("Disconnected from \(peripheral.identifier)")
        self.connectedDevices.removeValue(forKey: peripheral.toBluetoothDevice())
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
