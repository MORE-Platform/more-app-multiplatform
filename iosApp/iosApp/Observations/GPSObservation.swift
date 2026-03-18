//
//  GPSObservation.swift
//  iosApp
//
//  Created by Daniil Barkov on 17.03.23.
//  Copyright © 2023 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung
//  Licensed under the Apache 2.0 license with Commons Clause
//  (see https://www.apache.org/licenses/LICENSE-2.0 and
//  https://commonsclause.com/).
//

import CoreLocation
import Foundation
import UIKit
import shared

class GPSObservation: Observation_ {
    private let manager: CLLocationManager = CLLocationManager()
    private let locationDelegateProxy: LocationDelegateProxy
    public var currentLocation = CLLocation()

    init(repos: MainRepository, sensorPermissions: Set<String>) {
        self.locationDelegateProxy = LocationDelegateProxy(owner: nil)
        super.init(repos: repos, observationType: GPSType(sensorPermissions: sensorPermissions))

        self.locationDelegateProxy.owner = self

        if Thread.isMainThread {
            manager.delegate = locationDelegateProxy
        } else {
            DispatchQueue.main.async { [weak self] in
                guard let self else {
                    return
                }
                self.manager.delegate = self.locationDelegateProxy
            }
        }
        manager.desiredAccuracy = kCLLocationAccuracyBest
    }

    override func start() -> Bool {
        if hasPermission() == .granted {
            DispatchQueue.main.async {
                self.manager.allowsBackgroundLocationUpdates = true
                self.manager.showsBackgroundLocationIndicator = true
                self.manager.startUpdatingLocation()
            }
            return true
        }
        return false
    }

    override func stop(onCompletion: @escaping () -> Void) {
        manager.stopUpdatingLocation()
        onCompletion()
    }

    override func observerErrors() -> Set<String> {
        var errors: Set<String> = []
        if manager.authorizationStatus == .notDetermined {
            errors.insert("Permission request pending until observation is about to start")
            manager.requestWhenInUseAuthorization()
        } else if manager.authorizationStatus != .authorizedWhenInUse
            && manager.authorizationStatus != .authorizedAlways
        {
            errors.insert("Permission not granted to access location of the device")
            PermissionManager.openSensorPermissionDialog()
        } else if !CLLocationManager.locationServicesEnabled() {
            errors.insert("Location Services not enabled")
        }
        return errors
    }

    override func applyObservationConfig(settings: [String: Any]) {
    }
}

extension GPSObservation {
    fileprivate func handleLocations(_ locations: [CLLocation]) {
        let data = locations.compactMap { location in
            let dict = [
                "longitude": location.coordinate.longitude,
                "latitude": location.coordinate.latitude,
                "altitude": location.altitude,
            ]
            return ObservationBulkModel(data: dict, timestamp: Int64(location.timestamp.timeIntervalSince1970))
        }
        storeData(data: data) {}
    }

    fileprivate func handleAuthorizationChange(_ manager: CLLocationManager) {
        if manager.authorizationStatus == .restricted || manager.authorizationStatus == .denied || manager.accuracyAuthorization != .fullAccuracy {
            super.stopAndSetState(state: .active, scheduleId: nil)
        }
    }
}

private final class LocationDelegateProxy: NSObject, CLLocationManagerDelegate {
    weak var owner: GPSObservation?

    init(owner: GPSObservation?) {
        self.owner = owner
    }

    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        owner?.handleLocations(locations)
    }

    func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        owner?.handleAuthorizationChange(manager)
    }

    func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        print("Location error:", error)
    }
}
