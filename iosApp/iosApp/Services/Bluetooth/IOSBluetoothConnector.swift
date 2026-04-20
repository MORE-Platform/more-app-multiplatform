//
//  IOSBluetoothConnector.swift
//  More
//
//  Created by Jan Cortiel on 19.04.23.
//  Copyright © 2023 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung
//  Licensed under the Apache 2.0 license with Commons Clause
//  (see https://www.apache.org/licenses/LICENSE-2.0 and
//  https://commonsclause.com/).
//

import CoreBluetooth
import Foundation
import PolarBleSdk
import RxSwift
import shared

protocol BLEConnectorDelegate {
    func bleHasPower()
}

typealias BluetoothDeviceList = [BluetoothDeviceEntity: CBPeripheral]

class IOSBluetoothConnector: NSObject, BluetoothConnector {
    private let bleManager = BluetoothStateManagement.shared
    let specificBluetoothConnectors: KotlinMutableDictionary<NSString, BluetoothConnector> = KotlinMutableDictionary<NSString, BluetoothConnector>()

    private var peripherals: Set<CBPeripheral> = []

    private lazy var centralManager: CBCentralManager = {
        CBCentralManager(delegate: self, queue: nil)
    }()

    var observer: KotlinMutableSet<BluetoothConnectorObserver> = KotlinMutableSet()
    var delegate: BLEConnectorDelegate?

    private var scanningWithUnknownBLEState = false

    override init() {
        super.init()
        specificBluetoothConnectors.allValues
            .forEach {
                ($0 as? BluetoothConnector)?.observer.add(self)
            }
    }

    func addSpecificBluetoothConnector(key: String, connector: BluetoothConnector) {
        specificBluetoothConnectors[key] = connector
    }

    func connect(device: BluetoothDeviceEntity) -> KotlinError? {
        Napier.d("Connecting to device: \(device)")
        let (hasConnected, error) = connectToSpecificDevice(device: device)
        if hasConnected {
            guard let error else {
                return nil
            }
            Napier.e("\(error)")
            return error
        }
        if let cbPeripheral = peripherals.first(where: { $0.identifier.uuidString == device.deviceId }) {
            centralManager.connect(cbPeripheral)
            return nil
        } else {
            return KotlinError(message: "Could not find device")
        }
    }

    func disconnect(device: BluetoothDeviceEntity) {
        if let cbPeripheral = peripherals.first(where: { $0.identifier.uuidString == device.deviceId }) {
            centralManager.cancelPeripheralConnection(cbPeripheral)
        }
    }

    func addObserver(bluetoothConnectorObserver: BluetoothConnectorObserver) {
        observer.add(bluetoothConnectorObserver)
    }

    func removeObserver(bluetoothConnectorObserver: BluetoothConnectorObserver) {
        observer.remove(bluetoothConnectorObserver)
        if observer.count == 0 {
            stopScanning()
        }
    }

    func updateObserver(action: @escaping (BluetoothConnectorObserver) -> Void) {
        observer.forEach {
            if let observer = $0 as? BluetoothConnectorObserver {
                action(observer)
            }
        }
    }

    func scan() {
        if !bleManager.scanningValue {
            switch centralManager.state {
            case .unknown:
                Napier.w("Bluetooth state unknown")
                scanningWithUnknownBLEState = true
            case .resetting:
                Napier.w("Bluetooth state resetting")
            case .unsupported:
                Napier.w("Bluetooth state unsupported")
            case .unauthorized:
                Napier.w("Bluetooth state unauthorized")
            case .poweredOff:
                Napier.w("Bluetooth state powered off")
            case .poweredOn:
                Napier.i("Bluetooth state powered on")
                bleManager.isScanning(scan: true)
                centralManager.scanForPeripherals(withServices: nil, options: [CBCentralManagerScanOptionAllowDuplicatesKey: false])
            @unknown default:
                Napier.w("Bluetooth state unknown default")
            }
        }
    }

    func stopScanning() {
        centralManager.stopScan()
        bleManager.isScanning(scan: false)
    }

    func isConnectingToDevice(bluetoothDevice: BluetoothDeviceEntity) {
        updateObserver {
            $0.isConnectingToDevice(bluetoothDevice: bluetoothDevice)
        }
    }

    func didConnectToDevice(bluetoothDevice: BluetoothDeviceEntity) {
        updateObserver {
            $0.didConnectToDevice(bluetoothDevice: bluetoothDevice)
        }
    }

    func didDisconnectFromDevice(bluetoothDevice: BluetoothDeviceEntity) {
        updateObserver {
            $0.didDisconnectFromDevice(bluetoothDevice: bluetoothDevice)
        }
    }

    func didFailToConnectToDevice(bluetoothDevice: BluetoothDeviceEntity) {
        updateObserver {
            $0.didFailToConnectToDevice(bluetoothDevice: bluetoothDevice)
        }
    }

    func didDiscoverDevice(device: BluetoothDeviceEntity) {
        updateObserver {
            $0.didDiscoverDevice(device: device)
        }
    }

    func removeDiscoveredDevice(device: BluetoothDeviceEntity) {
        updateObserver {
            $0.removeDiscoveredDevice(device: device)
        }
    }

    func onBluetoothStateChange(bluetoothState: BluetoothState) {
        bleManager.setBluetoothState(active: bluetoothState == BluetoothState.on)
        if bluetoothState == BluetoothState.off {
            peripherals.removeAll()
        }
    }

    private func connectToSpecificDevice(device: BluetoothDeviceEntity) -> (Bool, KotlinError?) {
        if let connector =
            specificBluetoothConnectors
            .first(where: { device.deviceName?.lowercased().contains(($0.key as? String)?.lowercased() ?? "") ?? false })?.value as? BluetoothConnector
        {
            return (true, connector.connect(device: device))
        }
        return (false, nil)
    }

    private func disconnectFromSpecificDevice(device: BluetoothDeviceEntity) -> Bool {
        if let connector =
            specificBluetoothConnectors
            .first(where: { device.deviceName?.lowercased().contains(($0.key as? String)?.lowercased() ?? "") ?? false })?.value as? BluetoothConnector
        {
            connector.disconnect(device: device)
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
        Napier.i("Connected to \(peripheral.description)")
        let device = peripheral.toBluetoothDevice()
        peripherals.insert(peripheral)
        didConnectToDevice(bluetoothDevice: device)
    }

    func centralManager(_ central: CBCentralManager, didDisconnectPeripheral peripheral: CBPeripheral) {
        Napier.i("Disconnected from \(peripheral.identifier)")
        let device = peripheral.toBluetoothDevice()
        bleManager.removeConnectedDeviceIds(deviceIds: [peripheral.identifier.uuidString])
        didDisconnectFromDevice(bluetoothDevice: device)
    }

    func centralManager(_ central: CBCentralManager, didFailToConnect peripheral: CBPeripheral) {
        Napier.e("Did fail to connect to device: \(peripheral.identifier)")
        bleManager.removeConnectingDeviceIds(deviceIds: [peripheral.identifier.uuidString])
        didFailToConnectToDevice(bluetoothDevice: peripheral.toBluetoothDevice())
    }

    func centralManagerDidUpdateState(_ central: CBCentralManager) {
        Napier.d("Manager state is powered on: \(central.state == .poweredOn)")
        if central.state == .poweredOn {
            delegate?.bleHasPower()
            if scanningWithUnknownBLEState {
                scan()
            }
        }
        if central.state != .unknown {
            scanningWithUnknownBLEState = false
        }
    }

    func resetAll() {
        peripherals.removeAll()
    }

    internal func centralManager(_ central: CBCentralManager, didDiscover peripheral: CBPeripheral, advertisementData: [String: Any], rssi RSSI: NSNumber) {
        if peripheral.name != nil {
            let device = peripheral.toBluetoothDevice()
            if peripheral.state == .connected {
                peripherals.insert(peripheral)
                didConnectToDevice(bluetoothDevice: device)
            } else {
                peripherals.insert(peripheral)
                didDiscoverDevice(device: device)
            }
        }
    }
}

extension IOSBluetoothConnector: CBPeripheralDelegate {
}

extension CBPeripheral {
    func toBluetoothDevice() -> BluetoothDeviceEntity {
        BluetoothDeviceEntity.companion.create(
            deviceId: identifier.uuidString,
            deviceName: name ?? "Unknown",
            address: identifier.uuidString
        )
    }
}
