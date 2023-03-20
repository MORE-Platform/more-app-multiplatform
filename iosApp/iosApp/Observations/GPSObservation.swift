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
    private var permsManager = PermissionManager.permObj
    
    init(sensorPermissions: Set<String>) {
        super.init(observationTypeImpl: GPSType(sensorPermissions: sensorPermissions))
        manager = CLLocationManager()
        manager?.delegate = self
        manager?.desiredAccuracy = kCLLocationAccuracyBest
        permsManager.setGPSNeeded()
    }
    
    override func start() -> Bool {
        manager?.allowsBackgroundLocationUpdates = true
        manager?.showsBackgroundLocationIndicator = true
        manager?.requestAlwaysAuthorization()
        
        if (manager?.authorizationStatus == CLAuthorizationStatus.authorizedAlways) {
            manager?.startUpdatingLocation()
            return true
        } else {
            manager?.requestAlwaysAuthorization()
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
 
