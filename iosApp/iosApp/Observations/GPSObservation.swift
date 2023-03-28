//
//  GPSObservation.swift
//  iosApp
//
//  Created by Daniil Barkov on 17.03.23.
//  Copyright © 2023 orgName. All rights reserved.
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
        
        if (observerAccessible()) {
            manager.allowsBackgroundLocationUpdates = true
            manager.showsBackgroundLocationIndicator = true
            manager.requestAlwaysAuthorization()
            manager.startUpdatingLocation()
            return true
        } else {
            manager.requestAlwaysAuthorization()
        }
        return false
    }
    
    override func stop() {
        manager.stopUpdatingLocation()
    }
    
    override func observerAccessible() -> Bool {
        return CLLocationManager.locationServicesEnabled()
            && (manager.authorizationStatus == .authorizedWhenInUse
            || manager.authorizationStatus == .authorizedAlways)
    }
    
    override func applyObservationConfig(settings: Dictionary<String, Any>){
        
    }
    
    
}

extension GPSObservation: CLLocationManagerDelegate {
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        for location in locations {
            let dict = ["longitude": location.coordinate.longitude, "latitude": location.coordinate.latitude, "altitude": location.altitude]
            
            self.storeData(data: dict, timestamp: location.timestamp.millisecondsSince1970)
        }
    }
    
    func locationManagerDidPauseLocationUpdates(_ manager: CLLocationManager) {
        finish()
    }
}
 
