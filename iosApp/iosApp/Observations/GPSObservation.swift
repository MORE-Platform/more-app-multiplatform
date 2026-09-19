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
//  Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
//

import CoreLocation
import Foundation
import UIKit
import shared

class GPSObservation: Observation_ {
    private var manager: CLLocationManager?
    public var currentLocation = CLLocation()

    init(repos: MainRepository, sensorPermissions: Set<String>) {
        super.init(repos: repos, observationType: GPSType(sensorPermissions: sensorPermissions))
        Task { @MainActor [weak self] in
            self?.manager = CLLocationManager()
            self?.manager?.delegate = self
            
            self?.manager?.desiredAccuracy = kCLLocationAccuracyNearestTenMeters
            self?.manager?.activityType = .fitness
        }
    }

    
    override func start() -> Bool {
        Task { @MainActor [weak self] in
            if let manager = self?.manager {
                manager.allowsBackgroundLocationUpdates = true
                manager.showsBackgroundLocationIndicator = true
                manager.startUpdatingLocation()
                Napier.d("Started GPS location updates")
            }
        }
        
        return true
    }

    override func stop(onCompletion: @escaping () -> Void) {
        Napier.d("Stopping GPS location updates")
        Task { @MainActor [weak self] in
            self?.manager?.stopUpdatingLocation()
        }
        onCompletion()
    }

    override func observerErrors() -> Set<String> {
        var errors: Set<String> = []
        if !CLLocationManager.locationServicesEnabled() {
            errors.insert("Location Services not enabled")
        }
        return errors
    }

    override func applyObservationConfig(settings: [String: Any]) {
    }
    
}

extension GPSObservation: CLLocationManagerDelegate {
    func handleLocations(_ locations: [CLLocation]) {
        self.storeLocations(locations)
    }

    func handleAuthorizationChange(_ manager: CLLocationManager) {
        if manager.authorizationStatus == .restricted || manager.authorizationStatus == .denied || manager.accuracyAuthorization != .fullAccuracy {
            super.stopAndSetState(state: .paused, scheduleId: nil)
        }
    }
    
    @objc func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        self.storeLocations(locations)
    }
    
    @objc func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        Napier.e("Location update error \(error)")
    }
    
    @objc func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        self.handleAuthorizationChange(manager)
    }

    @objc func locationManager(_ manager: CLLocationManager, didChangeAuthorization status: CLAuthorizationStatus) {
        self.handleAuthorizationChange(manager)
    }
    
    private func storeLocations(_ locations: [CLLocation]) {
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
    

}
