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
//  Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
//

import Combine
import CoreBluetooth
import Foundation
import KMPNativeCoroutinesCombine
import PolarBleSdk
import RxSwift
import shared
import UIKit

class PolarVerityHeartRateObservation: Observation_ {
    private let deviceIdentificer: Set<String> = ["Polar"]
    private let polarConnector = AppDelegate.polarConnector

    private var connectedDevices: [BluetoothDeviceEntity] = []
    private var hrObservation: Disposable?

    private let bleManager = BluetoothStateManagement.shared

    private var deviceListener: AnyCancellable?

    private let errorStringTable = "Errors"

    private let polarController: PolarController

    private var cancellables = Set<AnyCancellable>()
    private static let notificationBackoffInterval: TimeInterval = 60
    private static var lastCannotStartNotificationDate: Date?

    init(repos: MainRepository, sensorPermissions: Set<String>) {
        polarController = PolarController(repos: repos)
        super.init(repos: repos, observationType: PolarVerityHeartRateType(sensorPermissions: sensorPermissions))

        createPublisher(for: polarController.hrFeatureChange)
        .receive(on: DispatchQueue.main)
        .sink(receiveCompletion: { _ in }) { pair in
            if let studyActive = pair.first?.boolValue, studyActive {
                if let hrReady = pair.second?.boolValue, hrReady {
                    print("HR Ready: \(hrReady)")
                    Task {
                        do {
                            try await AppDelegate.shared.observationManager.updateTaskStates()
                        } catch {
                            print("Cannot start polar observation: \(error)")
                        }
                    }
                } else {
                    AppDelegate.shared.observationManager.pauseObservationType(type: self.observationType.observationType)
                }
            }
        }
        .store(in: &cancellables)
    }

    override func start() -> Bool {
        if observerAccessible() {
            let acceptableDevices = bleManager.connectedDevicesValue.deviceWithNameIn(nameSet: deviceIdentificer)
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
                        showCannotStartNotificationWithBackoff(title: "Observation Error", message: "Error continuing Observation! There was a connection issue to a bluetooth sensor. Please make sure to enable bluetooth and connect all necessary devices!")
                        Observation_.pauseObservation(self.observationType)
                    }
                })
                return true
            }
        }
        showCannotStartNotificationWithBackoff(title: "Observation Error", message: "Cannot start Observation! Please make sure to enable Bluetooth and connect all necessary devices!")
        return false
    }

    override func stop(onCompletion: @escaping () -> Void) {
        hrObservation?.dispose()
        deviceListener?.cancel()
        onCompletion()
    }

    override func observerErrors() -> Set<String> {
        var errors: Set<String> = []
        if CBManager.authorization != .allowedAlways {
            errors.insert("Access to Bluetooth not granted")
            PermissionManager.openSensorPermissionDialog()
            PolarStates.shared.hrFeatureReady(ready: false)
        }
        if !bleManager.bluetoothActiveValue {
            errors.insert("Bluetooth is not enabled")
            PolarStates.shared.hrFeatureReady(ready: false)
        }
        if !AppDelegate.shared.bluetoothController.observerDeviceAccessible(bleDevices: deviceIdentificer) {
            PolarStates.shared.hrFeatureReady(ready: false)
            errors.insert("No polar device connected")
            errors.insert(Observation_.companion.ERROR_DEVICE_NOT_CONNECTED)
        } else if !PolarStates.shared.hrFeatureReadyValue {
            errors.insert("Heart-rate measurement feature unavailable")
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

    private func showCannotStartNotificationWithBackoff(title: String, message: String) {
        let now = Date()
        if let last = PolarVerityHeartRateObservation.lastCannotStartNotificationDate,
           now.timeIntervalSince(last) < PolarVerityHeartRateObservation.notificationBackoffInterval {
            return
        }
        PolarVerityHeartRateObservation.lastCannotStartNotificationDate = now
        showObservationErrorNotification(notificationBody: message, fallbackTitle: title)
    }

    private func listenToDeviceConnection() {
        deviceListener = createPublisher(for: bleManager.connectedDevices)
            .removeDuplicates()
            .receive(on: DispatchQueue.main)
            .sink(receiveCompletion: { _ in }, receiveValue: { [weak self] devices in
                if let self, !self.deviceIdentificer.anyNameIn(items: devices) {
                    PolarStates.shared.hrFeatureReady(ready: false)
                    self.deviceListener?.cancel()
                }
            })
    }
}
