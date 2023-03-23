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

class GPSObservation: Observation_, CLLocationManagerDelegate {
    
    private let manager: CLLocationManager = CLLocationManager()
    public var currentLocation = CLLocation()
    
    init(sensorPermissions: Set<String>) {
        super.init(observationType: GPSType(sensorPermissions: sensorPermissions))
        manager.delegate = self
        manager.desiredAccuracy = kCLLocationAccuracyNearestTenMeters
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
            || manager.authorizationStatus == .authorizedWhenInUse)
    }
    
    override func applyObservationConfig(settings: Dictionary<String, Any>){
        
    }
    
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        guard let first = locations.first else {
            return
        }
        
        let dict = ["longitude": first.coordinate.longitude, "latitude": first.coordinate.latitude, "altitude": first.altitude]
        
        self.storeData(data: dict, timestamp: -1)
    }
}
 
