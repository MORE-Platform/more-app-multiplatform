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
        
        self.polarApi.polarFilter(false)
        self.polarApi.observer = self
        self.polarApi.deviceInfoObserver = self
        self.polarApi.deviceFeaturesObserver = self
        self.polarApi.powerStateObserver = self
        self.polarApi.automaticReconnection = true
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
        if !scanning && self.observer != nil && bluetoothState == BluetoothState.on {
            print("Polar: Starting the scan...")
            scanning = true
            self.devicesSubscription = polarApi.searchForDevice().subscribe(onNext: { [weak self] device in
                if let self, !device.name.isEmpty {
                    self.didDiscoverDevice(device: BluetoothDevice.fromPolarDevice(polarInfo: device))
                }
            }, onError: { [weak self] error in
                print(error)
                self?.scanning = false
            }, onDisposed: { [weak self] in
                self?.scanning = false
            })
        }
    }

    func stopScanning() {
        if self.scanning {
            print("Polar: Stopping the scan and cleaning up...")
            self.devicesSubscription?.dispose()
            self.polarApi.cleanup()
            self.scanning = false
        }
    }

    func close() {
        applyObserver(bluetoothConnectorObserver: nil)
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
        } else {
            stopScanning()
        }
    }
    
    func replayStates() {
        print("Polar Connector: Replaying states...")
        onBluetoothStateChange(bluetoothState: self.bluetoothState)
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
        PolarVerityHeartRateObservation.setHRReady(ready: false)
        self.didDisconnectFromDevice(bluetoothDevice: BluetoothDevice.fromPolarDevice(polarInfo: identifier))
    }
}

extension PolarConnector: PolarBleApiPowerStateObserver {
    func blePowerOn() {
        print("Polar power on")
        self.onBluetoothStateChange(bluetoothState: .on)
        Task { [weak self] in
            self?.scan()
            try await Task.sleep(nanoseconds: 1_000_000_000)
            self?.stopScanning()
        }
    }
    
    func blePowerOff() {
        print("Polar power off")
        self.onBluetoothStateChange(bluetoothState: .off)
        self.connected.forEach { [weak self] device in
            if let device = device as? BluetoothDevice {
                self?.didDisconnectFromDevice(bluetoothDevice: device)
            }
        }
        self.discovered.forEach { [weak self] device in
            if let device = device as? BluetoothDevice {
                self?.removeDiscoveredDevice(device: device)
            }
        }
        stopScanning()
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
            PolarVerityHeartRateObservation.setHRReady(ready: true)
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
