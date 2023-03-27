//
//  PermissionManager.swift
//  iosApp
//
//  Created by Daniil Barkov on 06.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import Foundation
import CoreLocation
import AVFoundation

enum PermissionStatus {
    case accepted, declined, requesting
    
}

protocol PermissionManagerObserver {
    func accepted()
    func declined()
}

class PermissionManager: NSObject, ObservableObject {
    var observer: PermissionManagerObserver? = nil
    
    let locationManager: CLLocationManager = CLLocationManager()
    var gpsAuthorizationStatus: CLAuthorizationStatus?
    private var cameraPermissionGranted = false
    
    private var gpsStatus: PermissionStatus = .accepted
    private var gpsNeeded: Bool = false
    private var cameraNeeded: Bool = false
    
    var permissionsGranted: Bool = true

    
    override init() {
        super.init()
        gpsAuthorizationStatus = locationManager.authorizationStatus
        locationManager.delegate = self
        locationManager.desiredAccuracy = kCLLocationAccuracyBest

        
        setPermisssionValues(observationPermissions: IOSObservationFactory().sensorPermissions())
    }
    
    func requestGpsAuthorization(always: Bool = true) -> PermissionStatus {
        if self.locationManager.authorizationStatus == .notDetermined {
            if always {
                self.locationManager.requestAlwaysAuthorization()
            } else {
                self.locationManager.requestWhenInUseAuthorization()
            }
            return .requesting
        }
        
        else if(self.locationManager.authorizationStatus == CLAuthorizationStatus.denied
           || self.locationManager.authorizationStatus == CLAuthorizationStatus.restricted){
            observer?.declined()
            return .declined
        }
        return .accepted
    }
    
    func requestPermission() {
        if(self.gpsNeeded){
            gpsStatus = requestGpsAuthorization()
        }
        
        if (self.gpsStatus == .accepted) {
            observer?.accepted()
        }
    }
    
    private func checkAcceptedPerms() {
        if(self.cameraNeeded){
            permissionsGranted = (self.locationManager.authorizationStatus == CLAuthorizationStatus.authorizedAlways)
        }
    }

    
    func requestPermissionCamera() {
        AVCaptureDevice.requestAccess(for: .video, completionHandler: {accessGranted in
            DispatchQueue.main.async {
                self.cameraPermissionGranted = accessGranted
            }
        })
    }
    
    private func setPermisssionValues(observationPermissions: Set<String> = []) {
        gpsNeeded = observationPermissions.contains("gpsAlways")
        cameraNeeded = observationPermissions.contains("camera")
    }
}

extension PermissionManager: CLLocationManagerDelegate {
    func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        if manager.authorizationStatus == .restricted || manager.authorizationStatus == .denied {
            observer?.declined()
        }
        else if manager.authorizationStatus != .notDetermined {
            requestPermission()
        }
    }
}
