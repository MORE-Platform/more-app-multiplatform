//
//  Polar360Controller.swift
//  iosApp
//
//  Created on 2026-03-11.
//

import Foundation
import PolarBleSdk
import RxSwift
import shared

class Polar360Controller {
    static let shared = Polar360Controller()
    static let CONFIG_OFFLINE_RECORDING = "offline_recording"

    private let polarConnector = AppDelegate.polarConnector
    private let bleManager = BluetoothStateManagement.shared

    private var sdkModeEnabled = false
    private var currentDeviceId: String?

    private init() {}

    func findPolar360Device() -> BluetoothDeviceEntity? {
        return bleManager.connectedDevicesValue.first { device in
            guard let name = device.deviceName?.lowercased() else { return false }
            return name.contains("polar") && name.contains("360") && device.address != nil
        }
    }

    func ensureReady(deviceId: String, offlineMode: Bool) -> Completable {
        currentDeviceId = deviceId
        return checkIfDeviceIsSetup(identifier: deviceId)
            .andThen(offlineMode
                ? disableSdkModeIfNeeded(identifier: deviceId)
                : enableSdkModeIfNeeded(identifier: deviceId)
            )
    }

    func startOfflineRecording(deviceId: String, dataType: PolarDeviceDataType, settings: PolarSensorSetting? = nil) -> Disposable {
        return polarConnector.polarApi.startOfflineRecording(deviceId, feature: dataType, settings: settings, secret: nil)
            .subscribe(
                onCompleted: { NSLog("Polar360Controller: Started offline recording for \(dataType)") },
                onError: { error in NSLog("Polar360Controller: Failed to start offline recording for \(dataType): \(error)") }
            )
    }

    func stopOfflineRecording(dataType: PolarDeviceDataType) {
        //Todo when stopping offline recording in 1 specific need to be able to restart others that are not stopped
        // SO maybe two params fetching and restart data
        guard let deviceId = currentDeviceId else { return }
        _ = polarConnector.polarApi.stopOfflineRecording(deviceId, feature: dataType)
            .subscribe(
                onCompleted: { NSLog("Polar360Controller: Stopped offline recording for \(dataType)") },
                onError: { error in NSLog("Polar360Controller: Could not stop offline recording for \(dataType): \(error)") }
            )
    }

    func isOfflineRecordingMode(config: [String: Any]) -> Bool {
        if let val_ = config[Polar360Controller.CONFIG_OFFLINE_RECORDING] as? Bool {
            return val_
        }
        if let str = config[Polar360Controller.CONFIG_OFFLINE_RECORDING] as? String {
            return str.lowercased() == "true"
        }
        return false
    }

    func onDeviceDisconnected() {
        sdkModeEnabled = false
        currentDeviceId = nil
    }

    func getPolarApi() -> PolarBleApi {
        return polarConnector.polarApi
    }

    private func enableSdkModeIfNeeded(identifier: String) -> Completable {
        if sdkModeEnabled {
            return Completable.empty()
        }
        return Completable.deferred {
            do {
                self.sdkModeEnabled = true
                return try self.polarConnector.polarApi.enableSDKMode(identifier)
            } catch {
                return Completable.empty()
            }
        }
    }

    private func disableSdkModeIfNeeded(identifier: String) -> Completable {
        if !sdkModeEnabled {
            return Completable.empty()
        }
        return Completable.deferred {
            do {
                self.sdkModeEnabled = false
                return try self.polarConnector.polarApi.disableSDKMode(identifier)
            } catch {
                return Completable.empty()
            }
        }
    }

    private func checkIfDeviceIsSetup(identifier: String) -> Completable {
        return polarConnector.polarApi.isFtuDone(identifier)
            .flatMapCompletable { ftuDone in
                if ftuDone {
                    NSLog("Polar 360 FTU already done")
                    return Completable.empty()
                } else {
                    let dateFormatter = ISO8601DateFormatter()
                    dateFormatter.formatOptions = [.withInternetDateTime]
                    dateFormatter.timeZone = TimeZone(secondsFromGMT: 0)

                    let ftuConfig = PolarFirstTimeUseConfig(
                        gender: PolarFirstTimeUseConfig.Gender.female,
                        birthDate: Date(),
                        height: 180,
                        weight: 80,
                        maxHeartRate: 180,
                        vo2Max: 80,
                        restingHeartRate: 100,
                        trainingBackground: PolarFirstTimeUseConfig.TrainingBackground.frequent,
                        deviceTime: dateFormatter.string(from: Date()),
                        typicalDay: PolarFirstTimeUseConfig.TypicalDay.mostlyMoving,
                        sleepGoalMinutes: 480
                    )

                    return self.polarConnector.polarApi
                        .doFirstTimeUse(identifier, ftuConfig: ftuConfig)
                }
            }
    }
}
