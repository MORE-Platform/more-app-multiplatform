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

class PermissionManager: NSObject, ObservableObject, CLLocationManagerDelegate {
    
    var gpsAuthorizationStatus: CLAuthorizationStatus?
    var cameraPermissionGranted = false
    
    public var locationManager: CLLocationManager?
    
    var gpsNeeded: Bool
    var cameraNeeded: Bool
    
    var permissionsGranted: Bool
    var permissionsDenied: Bool
    
    static let permObj = PermissionManager()

    
    override private init() {
        locationManager = CLLocationManager()
        gpsAuthorizationStatus = locationManager?.authorizationStatus
        self.gpsNeeded = false
        self.cameraNeeded = false
        self.permissionsGranted = false
        self.permissionsDenied = false
        
        super.init()
        
        locationManager?.delegate = self
        locationManager?.desiredAccuracy = kCLLocationAccuracyBest
    }
    
    public func requestGpsAuthorization(always: Bool = true) {
        if always {
            self.locationManager?.requestAlwaysAuthorization()
        } else {
            self.locationManager?.requestWhenInUseAuthorization()
        }
        
        if(self.locationManager?.authorizationStatus == CLAuthorizationStatus.denied || self.locationManager?.authorizationStatus == CLAuthorizationStatus.restricted){
            permissionsDenied = true
        }
    }
    
    func requestPermission() {
        if(self.gpsNeeded){
            requestGpsAuthorization()
        }
        
        checkAcceptedPerms()
        
    }
    
    private func checkAcceptedPerms() {
        if(self.cameraNeeded){
            permissionsGranted = (self.locationManager?.authorizationStatus == CLAuthorizationStatus.authorizedAlways)
        }
    }
    
    func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        gpsAuthorizationStatus = manager.authorizationStatus
    }
    
    func requestPermissionCamera() {
        AVCaptureDevice.requestAccess(for: .video, completionHandler: {accessGranted in
            DispatchQueue.main.async {
                self.cameraPermissionGranted = accessGranted
            }
        })
    }
    
    public func setGPSNeeded() {
        self.gpsNeeded = true
    }
    
    public func setCameraNeeded() {
        self.cameraNeeded = true
    }
    
}
