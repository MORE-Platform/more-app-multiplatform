//
//  PolarConnector.swift
//  More
//
//  Created by Jan Cortiel on 24.04.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import CoreBluetooth
import Foundation
import PolarBleSdk
import RxSwift
import shared

class PolarConnector: NSObject, BluetoothConnector {
    var specificBluetoothConnectors: KotlinMutableDictionary<NSString, BluetoothConnector> = KotlinMutableDictionary()
    private var centralManager: CBCentralManager!
    
    var bluetoothState: BluetoothState = .off
    
    var discovered: KotlinMutableSet<BluetoothDevice> = KotlinMutableSet()
    var connected: KotlinMutableSet<BluetoothDevice> = KotlinMutableSet()
    
    var delegate: BLEConnectorDelegate?
    private var scanningWithUnknownBLEState = false
    private var devicesSubscription: Disposable? = nil
    var polarApi = PolarBleApiDefaultImpl
        .polarImplementation(DispatchQueue.main,
                             features: [
                                 .feature_hr,
                                 .feature_battery_info,
                                 .feature_device_info,
                                 .feature_polar_offline_recording,
                                 .feature_polar_online_streaming,
                                 .feature_polar_sdk_mode,
                                 .feature_polar_device_time_setup,
                             ])
    weak var observer: BluetoothConnectorObserver?
    
    var scanning = false {
        didSet {
            isScanning(boolean: scanning)
        }
    }
    
    override init() {
        super.init()
        centralManager = CBCentralManager(delegate: self, queue: nil)
        
        self.polarApi.polarFilter(false)
        self.polarApi.observer = self
        self.polarApi.deviceInfoObserver = self
        self.polarApi.deviceFeaturesObserver = self
        self.polarApi.powerStateObserver = self
    }
    
    func addSpecificBluetoothConnector(key: String, connector: BluetoothConnector) {
        specificBluetoothConnectors[key] = connector
    }

    func connect(device: BluetoothDevice) -> KotlinError? {
        if let deviceId = device.deviceId {
            do {
                try polarApi.connectToDevice(deviceId)
                return nil
            } catch {
                print(error)
                return KotlinError(message: error.localizedDescription)
            }
        }
        return KotlinError(message: "No valid device ID")
    }

    func disconnect(device: BluetoothDevice) {
        if let deviceId = device.deviceId {
            do {
                try polarApi.disconnectFromDevice(deviceId)
            } catch {
                print(error)
            }
        }
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
                onBluetoothStateChange(bluetoothState: .off)
            case .poweredOn:
                print("Bluetooth state powered on")
                scanning = true
                self.devicesSubscription = polarApi.searchForDevice().subscribe(onNext: { [weak self] device in
                    if let self, !device.name.isEmpty {
                        self.didDiscoverDevice(device: BluetoothDevice.fromPolarDevice(polarInfo: device))
                    }
                }, onError: { error in
                    print(error)
                }, onDisposed: { [weak self] in
                    self?.scanning = false
                })
            @unknown default:
                print("Bluetooth state unknown default")
            }
            
        }
    }

    func stopScanning() {
        print("Polar: Stopping the scan and cleaning up...")
        self.devicesSubscription?.dispose()
        self.polarApi.cleanup()
        self.scanning = false
    }

    func close() {
        stopScanning()
    }
    
    func isConnectingToDevice(bluetoothDevice: BluetoothDevice) {
        observer?.isConnectingToDevice(bluetoothDevice: bluetoothDevice)
    }
    
    func didConnectToDevice(bluetoothDevice: BluetoothDevice) {
        if let address = bluetoothDevice.address, !address.isEmpty && !connected.contains(where: { ($0 as? BluetoothDevice)?.address == address }) {
            connected.add(bluetoothDevice)
            removeDiscoveredDevice(device: bluetoothDevice)
        }
        observer?.didConnectToDevice(bluetoothDevice: bluetoothDevice)
    }
    
    func didDisconnectFromDevice(bluetoothDevice: BluetoothDevice) {
        if let device = connected.filter({ ($0 as? BluetoothDevice)?.address == bluetoothDevice.address}).first {
            connected.remove(device)
        }
        observer?.didDisconnectFromDevice(bluetoothDevice: bluetoothDevice)
    }
    
    func didFailToConnectToDevice(bluetoothDevice: BluetoothDevice) {
        observer?.didFailToConnectToDevice(bluetoothDevice: bluetoothDevice)
    }
    
    func removeDiscoveredDevice(device: BluetoothDevice) {
        if let address = device.address, let device = discovered.filter({($0 as? BluetoothDevice)?.address == address}).first {
            discovered.remove(device)
        }
        observer?.removeDiscoveredDevice(device: device)
    }
    
    func didDiscoverDevice(device: BluetoothDevice) {
        if let address = device.address, !address.isEmpty && !connected.contains(where: { ($0 as? BluetoothDevice)?.address == address }) && !discovered.contains(where: {($0 as? BluetoothDevice)?.address == address}) {
            discovered.add(device)
        }
        observer?.didDiscoverDevice(device: device)
    }
    
    func isScanning(boolean: Bool) {
        if boolean != scanning {
            scanning = boolean
        }
        observer?.isScanning(boolean: boolean)
    }
    
    func onBluetoothStateChange(bluetoothState: BluetoothState) {
        self.bluetoothState = bluetoothState
        observer?.onBluetoothStateChange(bluetoothState: bluetoothState)
    }
    
    func applyObserver(bluetoothConnectorObserver: BluetoothConnectorObserver?) {
        observer = bluetoothConnectorObserver
        if bluetoothConnectorObserver != nil {
            replayStates()
        }
    }
    
    func replayStates() {
        print("Polar Connector: Replaying states...")
        connected.forEach { self.didConnectToDevice(bluetoothDevice: $0 as! BluetoothDevice)}
        discovered.forEach{ self.didDiscoverDevice(device: $0 as! BluetoothDevice)}
        isScanning(boolean: scanning)
    }
    
}

extension PolarConnector: PolarBleApiObserver {
    func deviceConnecting(_ identifier: PolarBleSdk.PolarDeviceInfo) {
        print("Polar connecting: \(identifier.name)")
        self.isConnectingToDevice(bluetoothDevice: BluetoothDevice.fromPolarDevice(polarInfo: identifier))
    }
    
    func deviceConnected(_ identifier: PolarDeviceInfo) {
        print("Polar connected: \(identifier.name)")
        self.didConnectToDevice(bluetoothDevice: BluetoothDevice.fromPolarDevice(polarInfo: identifier))
    }
    
    func deviceDisconnected(_ identifier: PolarDeviceInfo) {
        print("Polar disconnected: \(identifier.name)")
        PolarVerityHeartRateObservation.hrReady = false
        self.didDisconnectFromDevice(bluetoothDevice: BluetoothDevice.fromPolarDevice(polarInfo: identifier))
    }
}

extension PolarConnector: PolarBleApiPowerStateObserver {
    func blePowerOn() {
        print("Polar power on")
    }
    
    func blePowerOff() {
        print("Polar power off")
    }
}

extension PolarConnector: PolarBleApiDeviceFeaturesObserver {
    // Deprecated
    func hrFeatureReady(_ identifier: String) {
        print("HR ready!")
    }
    
    // Deprecated
    func ftpFeatureReady(_ identifier: String) {
        print("FTP Feature ready!")
    }
    
    // Deprecated
    func streamingFeaturesReady(_ identifier: String, streamingFeatures: Set<PolarBleSdk.PolarDeviceDataType>) {
        print("Stream Features ready!")
    }
    
    func bleSdkFeatureReady(_ identifier: String, feature: PolarBleSdk.PolarBleSdkFeature) {
        if feature == .feature_hr {
            print("HR ready")
            PolarVerityHeartRateObservation.hrReady = true
        }
    }
    
    
}

extension PolarConnector: PolarBleApiDeviceInfoObserver {
    func batteryLevelReceived(_ identifier: String, batteryLevel: UInt) {
        print("Battery level for \(identifier): \(batteryLevel)")
    }
    
    func disInformationReceived(_ identifier: String, uuid: CBUUID, value: String) {
        print("Disinformation received by \(identifier): \(uuid); \(value)")
    }
   
}

extension PolarConnector: PolarBleApiLogger {
    func message(_ str: String) {
        print("Polar logger: \(str)")
    }
}

extension PolarConnector: CBCentralManagerDelegate {
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
}
