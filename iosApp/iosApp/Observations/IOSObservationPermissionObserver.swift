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

    func permissionStates(collectors: Any) async throws -> [String: PermissionApprovalState] {
        if let permissionCollectors = collectors as? [PermissionCollector] {
            return try await permissionStates(collectors: permissionCollectors)
        }
        return [:]
    }

    func permissionStates(collectors: [PermissionCollector]) async throws -> [String: PermissionApprovalState] {
        if collectors.isEmpty {
            return [:]
        }

        var pairs: [(String, PermissionApprovalState)] = []
        pairs.reserveCapacity(collectors.count)

        try await withThrowingTaskGroup(of: (String, PermissionApprovalState).self) { group in
            for collector in collectors {
                group.addTask {
                    let state = try await collector.permissionState()
                    return (collector.permissionKey, state)
                }
            }

            for try await pair in group {
                pairs.append(pair)
            }
        }

        return Dictionary(uniqueKeysWithValues: pairs)
    }


    func requestPermissions(collectors: Any) async throws {
        if let permissionCollectors = collectors as? [PermissionCollector] {
            try await requestPermissions(collectors: permissionCollectors)
        }
    }


    @MainActor func requestPermissions(collectors: [PermissionCollector]) async throws {
        guard !collectors.isEmpty else { return }
        AppDelegate.shared.observationFactory.startRequestingPermissions()
        defer {
            AppDelegate.shared.observationFactory.stopRequestingPermissions()
        }

        let bundled = collectors.compactMap { $0 as? BundledPermissionCollector }
        let nonBundled = collectors.filter { !($0 is BundledPermissionCollector) }

        let grouped = Dictionary(grouping: bundled, by: { $0.permissionGroup })
        for (group, groupCollectors) in grouped {
            if group == ConstantsKt.HEALTH_COLLECTOR_GROUP {
                let healthCollectors = groupCollectors.compactMap { $0 as? HealthConnectCollector }
                let metrics = Set(healthCollectors.flatMap { HealthKitManager.metrics(for: $0.dataType) })
                if !metrics.isEmpty {
                    try await HealthKitManager.shared.requestPermissions(for: metrics)
                }
            } else {
                for collector in groupCollectors {
                    try await collector.requestPermission()
                }
            }
        }

        for collector in nonBundled {
            try await collector.requestPermission()
        }
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
