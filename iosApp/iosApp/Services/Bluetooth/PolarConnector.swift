//
//  PolarConnector.swift
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

import CoreBluetooth
import Foundation
import PolarBleSdk
import RxSwift
import shared
import UIKit

class PolarConnector: NSObject, BluetoothConnector {
    private let bleManager = BluetoothStateManagement.shared
    var specificBluetoothConnectors: KotlinMutableDictionary<NSString, BluetoothConnector> = KotlinMutableDictionary()

    var delegate: BLEConnectorDelegate?
    private var scanningWithUnknownBLEState = false
    private var devicesSubscription: Disposable?

    private(set) var polarApi: PolarBleApi

    override init() {
        polarApi = PolarBleApiDefaultImpl.polarImplementation(
            DispatchQueue.main,
            features: [
                .feature_hr,
                .feature_battery_info,
                .feature_device_info,
                .feature_polar_offline_recording,
                .feature_polar_online_streaming,
                .feature_polar_sdk_mode,
                .feature_polar_device_time_setup,
            ]
        )

        super.init()

        polarApi.observer = self
        polarApi.polarFilter(true)
        polarApi.deviceInfoObserver = self
        polarApi.deviceFeaturesObserver = self
        polarApi.powerStateObserver = self

//        Enable for Debug Logging
//        polarApi.logger = self

        bleManager.setBluetoothState(active: polarApi.isBlePowered)
    }

    var observer: KotlinMutableSet<BluetoothConnectorObserver> = KotlinMutableSet()

    func addSpecificBluetoothConnector(key: String, connector: BluetoothConnector) {
        specificBluetoothConnectors[key] = connector
    }

    func connect(device: BluetoothDeviceEntity) -> KotlinError? {
        let performConnect = { () -> KotlinError? in
            do {
                self.stopScanning()
                try self.polarApi.connectToDevice(device.deviceId)
                return nil
            } catch {
                Napier.e("\(error)")
                return KotlinError(message: error.localizedDescription)
            }
        }

        if Thread.isMainThread {
            return performConnect()
        } else {
            var result: KotlinError?
            DispatchQueue.main.sync {
                result = performConnect()
            }
            return result
        }
    }

    func disconnect(device: BluetoothDeviceEntity) {
        let performConnect = { () in
            do {
                self.stopScanning()
                try self.polarApi.disconnectFromDevice(device.deviceId)
            } catch {
                Napier.e("\(error)")
            }
        }

        if Thread.isMainThread {
            performConnect()
        } else {
            Task { @MainActor in
                performConnect()
            }
        }
    }

    func scan() {
        if CBManager.authorization == .restricted || CBManager.authorization == .denied {
            PermissionManager.openSensorPermissionDialog()
        } else if !bleManager.scanningValue && observer.count > 0 && bleManager.bluetoothActiveValue && bleManager.devicesCurrentlyConnectingValue.isEmpty {
            Napier.d("Polar: Starting the scan...")
            bleManager.isScanning(scan: true)
            Task { @MainActor [weak self] in
                if let self {
                    self.devicesSubscription = self.polarApi.searchForDevice().subscribe(onNext: { device in
                        self.didDiscoverDevice(device: BluetoothDeviceEntity.fromPolarDevice(polarInfo: device))
                    }, onError: { error in
                        Napier.e("\(error)")
                        BluetoothStateManagement.shared.isScanning(scan: false)
                    }, onDisposed: {
                        BluetoothStateManagement.shared.isScanning(scan: false)
                    })
                }
            }
        }
    }

    func stopScanning() {
        Task { @MainActor [weak self] in
            if let self, BluetoothStateManagement.shared.scanningValue {
                Napier.d("Polar: Stopping the scan and cleaning up...")
                self.devicesSubscription?.dispose()
                self.devicesSubscription = nil

                BluetoothStateManagement.shared.isScanning(scan: false)
            }
        }
    }

    func close() {
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
        if bleManager.connectedDevicesValue.map({ $0.deviceName?.lowercased().contains("polar") }).isEmpty {
            PolarStates.shared.hrFeatureReady(ready: false)
        }
    }

    func didFailToConnectToDevice(bluetoothDevice: BluetoothDeviceEntity) {
        updateObserver {
            $0.didFailToConnectToDevice(bluetoothDevice: bluetoothDevice)
        }
    }

    func removeDiscoveredDevice(device: BluetoothDeviceEntity) {
        updateObserver {
            $0.removeDiscoveredDevice(device: device)
        }
    }

    func didDiscoverDevice(device: BluetoothDeviceEntity) {
        updateObserver {
            $0.didDiscoverDevice(device: device)
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

    func resetAll() {
        polarApi.cleanup()
    }
}

extension PolarConnector: PolarBleApiObserver {
    func deviceDisconnected(_ identifier: PolarBleSdk.PolarDeviceInfo, pairingError: Bool) {
        Napier.i("Polar disconnected: \(identifier.name). Had paring error: \(pairingError)")
        didDisconnectFromDevice(bluetoothDevice: BluetoothDeviceEntity.fromPolarDevice(polarInfo: identifier))
    }

    func deviceConnecting(_ identifier: PolarBleSdk.PolarDeviceInfo) {
        Napier.i("Polar connecting: \(identifier.name)")
        isConnectingToDevice(bluetoothDevice: BluetoothDeviceEntity.fromPolarDevice(polarInfo: identifier))
    }

    func deviceConnected(_ identifier: PolarDeviceInfo) {
        Napier.i("Polar connected: \(identifier.name)")
        didConnectToDevice(bluetoothDevice: BluetoothDeviceEntity.fromPolarDevice(polarInfo: identifier))
    }
}

extension PolarConnector: PolarBleApiPowerStateObserver {
    func blePowerOn() {
        Napier.i("Polar power on")
        bleManager.setBluetoothState(active: true)
    }

    func blePowerOff() {
        Napier.i("Polar power off")
        bleManager.setBluetoothState(active: false)
    }
}

extension PolarConnector: PolarBleApiDeviceFeaturesObserver {
    func bleSdkFeatureReady(_ identifier: String, feature: PolarBleSdk.PolarBleSdkFeature) {
        if feature == .feature_hr {
            Napier.i("Polar HR Feature ready!")
            PolarStates.shared.hrFeatureReady(ready: true)
        }
        if feature == .feature_polar_sdk_mode {
            Napier.i("Polar SDK Mode ready!")
            PolarStates.shared.sdkModeReady(ready: true)
        }
    }
}

extension PolarConnector: PolarBleApiDeviceInfoObserver {
    func batteryChargingStatusReceived(_ identifier: String, chargingStatus: PolarBleSdk.BleBasClient.ChargeState) {
        Napier.d("Battery charging status received by \(identifier): \(chargingStatus)")
    }

    func disInformationReceivedWithKeysAsStrings(_ identifier: String, key: String, value: String) {
        Napier.d("DisinformationReceivedWithKeysAsString by \(identifier): \(key); \(value)")
    }

    func batteryLevelReceived(_ identifier: String, batteryLevel: UInt) {
        Napier.d("Battery level for \(identifier): \(batteryLevel)")
    }

    func disInformationReceived(_ identifier: String, uuid: CBUUID, value: String) {
        Napier.d("Disinformation received by \(identifier): \(uuid); \(value)")
    }
}

extension PolarConnector: PolarBleApiLogger {
    func message(_ str: String) {
        Napier.d("Polar logger: \(str)")
    }
}
