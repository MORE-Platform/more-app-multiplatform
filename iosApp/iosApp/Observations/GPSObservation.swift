//
//  GPSObservation.swift
//  iosApp
//
//  Created by Daniil Barkov on 17.03.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import Foundation
import shared
import CoreLocation

class GPSObservation: Observation_ {
    
    private let manager: CLLocationManager = CLLocationManager()
    public var currentLocation = CLLocation()
    
    init(sensorPermissions: Set<String>) {
        super.init(observationType: GPSType(sensorPermissions: sensorPermissions))
        manager.delegate = self
        manager.desiredAccuracy = kCLLocationAccuracyBest
    }
    
    override func start() -> Bool {
        Task {
            if (observerAccessible()) {
                manager.allowsBackgroundLocationUpdates = true
                manager.showsBackgroundLocationIndicator = true
                manager.requestAlwaysAuthorization()
                manager.startUpdatingLocation()
            } else {
                manager.requestAlwaysAuthorization()
                stopAndSetState(state: .active)
            }
        }
        return true
    }
    
    override func stop(onCompletion: @escaping () -> Void) {
        manager.stopUpdatingLocation()
        onCompletion()
    }
    
    override func observerAccessible() -> Bool {
        return CLLocationManager.locationServicesEnabled()
            && (manager.authorizationStatus == .authorizedWhenInUse
            || manager.authorizationStatus == .authorizedAlways)
    }
    
    override func applyObservationConfig(settings: Dictionary<String, Any>){}
    
    
    override func needsToRestartAfterAppClosure() -> Bool {
        true
    }
}

extension GPSObservation: CLLocationManagerDelegate {
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        let data = locations.compactMap { location in
            let dict = ["longitude": location.coordinate.longitude, "latitude": location.coordinate.latitude, "altitude": location.altitude]
            return ObservationBulkModel(data: dict, timestamp: Int64(location.timestamp.timeIntervalSince1970))
        }
        self.storeData(data: data) {}
    }
    
    func locationManagerDidPauseLocationUpdates(_ manager: CLLocationManager) {
        finish()
    }
    
    func locationManagerDidResumeLocationUpdates(_ manager: CLLocationManager) {
        if isRunning() {
            _ = start()
        }
    }
}
 
