//
//  PolarVerityHeartRateObservation.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 28.03.23.
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

class PolarVerityHeartRateObservation: Observation_ {
    static var hrReady = false

    static func setHRReady() {
        if !hrReady {
            hrReady = true
            AppDelegate.shared.observationManager.startObservationType(type: PolarVerityHeartRateType(sensorPermissions: []).observationType)
        }
    }

    private let deviceIdentificer: Set<String> = ["Polar"]
    private let polarConnector = AppDelegate.polarConnector

    private var connectedDevices: [BluetoothDevice] = []
    private var hrObservation: Disposable?

    private let deviceManager = BluetoothDeviceManager.shared

    private var deviceListener: Ktor_ioCloseable?
    
    private let errorStringTable = "Errors"

    init(sensorPermissions: Set<String>) {
        super.init(observationType: PolarVerityHeartRateType(sensorPermissions: sensorPermissions))
    }

    override func start() -> Bool {
        if observerAccessible() {
            let acceptableDevices = deviceManager.connectedDevicesAsValue().deviceWithNameIn(nameSet: deviceIdentificer)
            if !acceptableDevices.isEmpty, let firstAddres = acceptableDevices[0].address {
                listenToDeviceConnection()
                hrObservation = polarConnector.polarApi.startHrStreaming(firstAddres).subscribe(onNext: { [weak self] data in
                    if let self, let hrData = data.first {
                        self.storeData(data: ["hr": hrData.hr], timestamp: -1) {
                        }
                    }
                }, onError: { [weak self] error in
                    print(error)
                    if let self {
                        showObservationErrorNotification(notificationBody: "Error continuing Observation! There was a connection issue to a bluetooth sensor. Please make sure to enable bluetooth and connect all necessary devices!".localize(withComment: "Error continuing Observation! There was a connection issue to a bluetooth sensor. Please make sure to enable bluetooth and connect all necessary devices!", useTable: errorStringTable), fallbackTitle: "Observation Error".localize(withComment: "Observation Error", useTable: errorStringTable))
                        self.pauseObservation(self.observationType)
                    }
                })
                return true
            }
        }
        showObservationErrorNotification(notificationBody: "Cannot start Observation! Please make sure to enable Bluetooth and connect all necessary devices!".localize(withComment: "Cannot start Observation! Please make sure to enable Bluetooth and connect all necessary devices!", useTable: errorStringTable), fallbackTitle: "Observation Error".localize(withComment: "Observation Error", useTable: errorStringTable))
        return false
    }

    override func stop(onCompletion: @escaping () -> Void) {
        hrObservation?.dispose()
        deviceListener?.close()
        onCompletion()
    }

    override func observerErrors() -> Set<String> {
        var errors: Set<String> = []
        let state = AppDelegate.shared.bluetoothController.bluetoothPower.value as? BluetoothState
        if CBManager.authorization != .allowedAlways {
            errors.insert("Access to Bluetooth not granted")
            PermissionManager.openSensorPermissionDialog()
        }
        if state == nil || state == BluetoothState.off  {
            errors.insert("Bluetooth is not enabled")
        }
        if !AppDelegate.shared.bluetoothController.observerDeviceAccessible(bleDevices: deviceIdentificer) {
            errors.insert("No polar device connected")
            errors.insert(Observation_.companion.ERROR_DEVICE_NOT_CONNECTED)
        }
        return errors
    }

    override func applyObservationConfig(settings: Dictionary<String, Any>) {
    }

    override func bleDevicesNeeded() -> Set<String> {
        print("Polar device needed \(deviceIdentificer)")
        return deviceIdentificer
    }

    override func ableToAutomaticallyStart() -> Bool {
        observerAccessible()
    }

    private func listenToDeviceConnection() {
        deviceListener = deviceManager.connectedDevicesAsClosure { [weak self] devices in
            if let self, !self.deviceIdentificer.anyNameIn(items: devices) {
                self.pauseObservation(self.observationType)
                PolarVerityHeartRateObservation.hrReady = false
                self.deviceListener?.close()
            }
        }
    }
}
