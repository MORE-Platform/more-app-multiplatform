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
import CoreMotion

enum PermissionStatus {
    case accepted, declined, requesting, non
    
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
    
    private var gpsStatus: PermissionStatus = .non
    private var gpsNeeded: Bool = false
    private var cmSensorRecorderNeeded = false
    private var cmSensorStatus: PermissionStatus = .non {
        didSet {
            if cmSensorStatus == .accepted {
                requestPermission()
            } else if cmSensorStatus == .declined {
                observer?.declined()
            }
        }
    }
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
                || self.locationManager.authorizationStatus == CLAuthorizationStatus.restricted || self.locationManager.accuracyAuthorization != .fullAccuracy){
            observer?.declined()
            return .declined
        }
        return .accepted
    }
    
    func requestCMSensorRecorder() {
        let status = CMSensorRecorder.authorizationStatus()
        if status == .notDetermined {
            let activityManager = CMMotionActivityManager()
            activityManager.startActivityUpdates(to: OperationQueue.main) { activity in
                self.cmSensorStatus = .accepted
                activityManager.stopActivityUpdates()
            }
            let timer = Timer.scheduledTimer(withTimeInterval: 0.3, repeats: true) { timer in
                let status = CMSensorRecorder.authorizationStatus()
                if status == .authorized {
                    timer.invalidate()
                } else if status == .restricted || status == .denied {
                    self.cmSensorStatus = .declined
                    timer.invalidate()
                }
            }
        }
    }
    
    func requestPermission() {
        if self.gpsNeeded && gpsStatus != .accepted {
            gpsStatus = requestGpsAuthorization()
        }
        else if self.cmSensorRecorderNeeded && self.cmSensorStatus != .accepted {
            requestCMSensorRecorder()
        }
        else {
            observer?.accepted()
        }
    }
    
    private func checkAcceptedPerms() {
        if self.cameraNeeded {
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
        cmSensorRecorderNeeded = observationPermissions.contains("cmsensorrecorder")
    }
}

extension PermissionManager: CLLocationManagerDelegate {
    func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        if manager.authorizationStatus == .restricted || manager.authorizationStatus == .denied || manager.accuracyAuthorization != .fullAccuracy {
            observer?.declined()
        }
        else if manager.authorizationStatus != .notDetermined {
            self.gpsStatus = .accepted
            requestPermission()
        }
    }
}
