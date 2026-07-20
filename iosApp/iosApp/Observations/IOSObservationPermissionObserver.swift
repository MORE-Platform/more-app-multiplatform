//
//  IOSObservationPermissionObserver.swift
//  iosApp
//
//  Created by Junie on 10.03.26.
//

import AppTrackingTransparency
import CoreBluetooth
import CoreLocation
import CoreMotion
import Foundation
import shared

class IOSObservationPermissionObserver: NSObject, ObservationPermissionObserver {
    private let locationManager = CLLocationManager()
    private var centralManager: CBCentralManager?
    private var motionActivityManager: CMMotionActivityManager?

    func permissionState(observationType: ObservationType) -> PermissionApprovalState {
        if observationType is GPSType {
            let lm = CLLocationManager()
            let status = lm.authorizationStatus
            Napier.i("GPS authorization: \(status)")
            return if status == .notDetermined {
                .notSet
            } else if (status == .authorizedAlways || status == .authorizedWhenInUse) && lm.accuracyAuthorization == .fullAccuracy {
                .granted
            } else {
                .declined
            }
        }
        if observationType is AccelerometerType {
            let status = CMSensorRecorder.authorizationStatus()
            Napier.i("CMSensorRecorder authorization: \(status)")
            return if status == .notDetermined {
                .notSet
            } else if status == .authorized {
                .granted
            } else {
                .declined
            }
        }
        if observationType is PolarVerityHeartRateType {
            let status = CBManager.authorization
            Napier.i("CBManager authorization: \(status)")
            return if status == .notDetermined {
                .notSet
            } else if status == .allowedAlways {
                .granted
            } else {
                .declined
            }
        }
        if observationType is AppUsageObservationType {
            let status = ATTrackingManager.trackingAuthorizationStatus
            Napier.i("ATTrackingManager authorization: \(status)")
            if status == .notDetermined {
                return .notSet
            } else if status == .authorized {
                Napier.event(.appTrackingAccepted)
                return .granted
            } else {
                Napier.event(.appTrackingDeclined)
                return .declined
            }
        }
        return .granted
    }

    func requestPermission(observationType: ObservationType) {
        AppDelegate.shared.observationFactory.startRequestingPermissions()
        if observationType is GPSType {
            locationManager.delegate = self
            let status = locationManager.authorizationStatus
            if status == .notDetermined {
                // Request both When In Use and Always (if available in Info.plist)
                locationManager.requestWhenInUseAuthorization()
                locationManager.requestAlwaysAuthorization()
            } else if status == .denied || status == .restricted || locationManager.accuracyAuthorization != .fullAccuracy {
                PermissionManager.openSensorPermissionDialog()
                AppDelegate.shared.observationFactory.stopRequestingPermissions()
            } else {
                AppDelegate.shared.observationFactory.stopRequestingPermissions()
            }
            return
        }

        if observationType is AccelerometerType {
            let status = CMSensorRecorder.authorizationStatus()
            if status == .notDetermined {
                // Trigger the motion permission prompt by starting activity updates briefly
                let mam = CMMotionActivityManager()
                motionActivityManager = mam
                mam.startActivityUpdates(to: OperationQueue.main) { [weak self] _ in
                    self?.motionActivityManager?.stopActivityUpdates()
                    self?.motionActivityManager = nil
                    AppDelegate.shared.observationFactory.stopRequestingPermissions()
                }
            } else if status == .denied || status == .restricted {
                PermissionManager.openSensorPermissionDialog()
                AppDelegate.shared.observationFactory.stopRequestingPermissions()
            } else {
                AppDelegate.shared.observationFactory.stopRequestingPermissions()
            }
            return
        }

        if observationType is PolarVerityHeartRateType {
            let status = CBManager.authorization
            if status == .notDetermined {
                // Creating a CBCentralManager triggers the Bluetooth permission prompt
                centralManager = CBCentralManager(delegate: self, queue: nil)
            } else if status == .denied || status == .restricted {
                PermissionManager.openSensorPermissionDialog()
                AppDelegate.shared.observationFactory.stopRequestingPermissions()
            } else {
                AppDelegate.shared.observationFactory.stopRequestingPermissions()
            }
            return
        }

        if observationType is AppUsageObservationType {
            let status = ATTrackingManager.trackingAuthorizationStatus
            if status == .notDetermined {
                ATTrackingManager.requestTrackingAuthorization { _ in
                    // Observation flow will re-check via permissionState
                    AppDelegate.shared.observationFactory.stopRequestingPermissions()
                }
            } else if status == .denied || status == .restricted {
                PermissionManager.openSensorPermissionDialog()
                AppDelegate.shared.observationFactory.stopRequestingPermissions()
            } else {
                AppDelegate.shared.observationFactory.stopRequestingPermissions()
            }
            return
        }

        // Default fallback for other observation types
        PermissionManager.openSensorPermissionDialog()
        AppDelegate.shared.observationFactory.stopRequestingPermissions()
    }
}

extension IOSObservationPermissionObserver: CLLocationManagerDelegate {
    func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        // No-op: Observation flow will re-check via permissionState
        locationManager.delegate = nil
        AppDelegate.shared.observationFactory.stopRequestingPermissions()
    }
}

extension IOSObservationPermissionObserver: CBCentralManagerDelegate {
    func centralManagerDidUpdateState(_ central: CBCentralManager) {
        // Release once we have a state update; prompt has been shown (if needed)
        centralManager = nil
        AppDelegate.shared.observationFactory.stopRequestingPermissions()
    }
}
