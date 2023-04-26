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

class PolarConnector: BluetoothConnector {
    
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
    var observer: BluetoothConnectorObserver?
    
    private var scanning = false
    
    init() {
        self.polarApi.polarFilter(false)
        self.polarApi.observer = self
        self.polarApi.deviceInfoObserver = self
        self.polarApi.deviceFeaturesObserver = self
        self.polarApi.powerStateObserver = self
        self.polarApi.logger = self
    }
    

    var specificBluetoothConnectors: [String: BluetoothConnector] = [:]

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

    func isScanning() -> Bool {
        scanning
    }

    func scan() {
        if !scanning {
            scanning = true
            self.devicesSubscription = polarApi.searchForDevice().subscribe(onNext: { [weak self] device in
                if let self {
                    self.discoveredDevice(device: BluetoothDevice.fromPolarDevice(polarInfo: device))
                }
            }, onError: { error in
                print(error)
            }, onDisposed: { [weak self] in
                self?.scanning = false
            })
        }
    }

    func stopScanning() {
        self.devicesSubscription?.dispose()
        self.scanning = false
    }

    func close() {
        stopScanning()
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
    
}

extension PolarConnector: PolarBleApiObserver {
    func deviceConnecting(_ identifier: PolarBleSdk.PolarDeviceInfo) {
        print("Polar connecting: \(identifier.name)")
        observer?.isConnectingToDevice(bluetoothDevice: BluetoothDevice.fromPolarDevice(polarInfo: identifier))
    }
    
    func deviceConnected(_ identifier: PolarDeviceInfo) {
        print("Polar connected: \(identifier.name)")
        observer?.didConnectToDevice(bluetoothDevice: BluetoothDevice.fromPolarDevice(polarInfo: identifier))
    }
    
    func deviceDisconnected(_ identifier: PolarDeviceInfo) {
        print("Polar disconnected: \(identifier.name)")
        PolarVerityHeartRateObservation.hrReady = false
        observer?.didDisconnectFromDevice(bluetoothDevice: BluetoothDevice.fromPolarDevice(polarInfo: identifier))
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
