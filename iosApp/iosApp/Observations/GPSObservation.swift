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
    private var running = false
    
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
                running = true
                stopAndSetState(state: .active, scheduleId: nil)
            }
        }
        return true
    }
    
    override func stop(onCompletion: @escaping () -> Void) {
        manager.stopUpdatingLocation()
        running = false
        onCompletion()
    }
    
    override func observerAccessible() -> Bool {
        return CLLocationManager.locationServicesEnabled()
            && (manager.authorizationStatus == .authorizedWhenInUse
            || manager.authorizationStatus == .authorizedAlways)
    }
    
    override func applyObservationConfig(settings: Dictionary<String, Any>){}
    
    
    override func needsToRestartAfterAppClosure() -> Bool {
        !running
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
}
 
