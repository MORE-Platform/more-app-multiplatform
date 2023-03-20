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
    private var manager: CLLocationManager?
    public var currentLocation = CLLocation()
    
    init(sensorPermissions: Set<String>) {
        super.init(observationTypeImpl: GPSType(sensorPermissions: sensorPermissions))
        manager = CLLocationManager()
        manager?.delegate = self
        manager?.desiredAccuracy = kCLLocationAccuracyBest
        
    }
    
    override func start() -> Bool {
        manager?.allowsBackgroundLocationUpdates = true
        manager?.requestAlwaysAuthorization()
        manager?.showsBackgroundLocationIndicator = true
        if ((manager?.allowsBackgroundLocationUpdates) != nil) {
            manager?.requestAlwaysAuthorization()
            manager?.startUpdatingLocation()
            return true
        }
        return false
    }
    
    override func stop() {
        manager?.stopUpdatingLocation()
    }
    
    override func observerAccessible() -> Bool {
        ((manager?.isAccessibilityElement) != nil)
    }
    
    override func applyObservationConfig(settings: Dictionary<String, Any>){
        
    }
    
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        guard let first = locations.first else {
            return
        }
        
        let dict = ["longitude": first.coordinate.longitude, "latitude": first.coordinate.latitude, "altitude": first.altitude]
        
        self.storeData(data: dict)
    }
}
 
