//
//  PermissionManager.swift
//  iosApp
//
//  Created by Daniil Barkov on 06.03.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import AVFoundation
import CoreBluetooth
import CoreLocation
import CoreMotion
import Foundation
import UserNotifications
import UIKit

enum PermissionStatus {
    case accepted, declined, requesting, non
}

protocol PermissionManagerObserver {
    func accepted()
    func declined()
}

class PermissionManager: NSObject, ObservableObject {
    var observer: PermissionManagerObserver?
    private var permissionsRequested = false

    private let locationManager: CLLocationManager = CLLocationManager()

    private var cameraPermissionGranted = false

    private var gpsStatus: PermissionStatus = .non {
        didSet {
            if gpsStatus == .accepted {
                locationManager.delegate = nil
                requestPermission()
            } else if gpsStatus == .declined {
                locationManager.delegate = nil
                observer?.declined()
            } else if gpsStatus == .requesting {
                locationManager.delegate = self
                locationManager.desiredAccuracy = kCLLocationAccuracyBest
            }
        }
    }

    private var cmSensorStatus: PermissionStatus = .non {
        didSet {
            if cmSensorStatus == .accepted {
                requestPermission()
            } else if cmSensorStatus == .declined {
                observer?.declined()
            }
        }
    }
    
    private var notificationStatus: PermissionStatus = .non {
        didSet {
            if notificationStatus == .accepted {
                AppDelegate.registerForNotifications()
                requestPermission()
            } else if notificationStatus == .declined {
                observer?.declined()
            }
        }
    }

    private var cameraStatus: PermissionStatus = .non {
        didSet {
            if cameraStatus == .accepted {
                requestPermission()
            } else if cameraStatus == .declined {
                observer?.declined()
            }
        }
    }

    private var bluetoothStatus: PermissionStatus = .non {
        didSet {
            if bluetoothStatus == .accepted {
                requestPermission()
            } else if bluetoothStatus == .declined {
                observer?.declined()
            }
        }
    }
    

    var permissionsGranted: Bool = true

    override init() {
        super.init()
        setPermisssionValues(observationPermissions: IOSObservationFactory().sensorPermissions())
    }

    private func requestGpsAuthorization(always: Bool = true) {
        if locationManager.authorizationStatus == .notDetermined {
            if always {
                locationManager.requestAlwaysAuthorization()
            } else {
                locationManager.requestWhenInUseAuthorization()
            }
            gpsStatus = .requesting
        } else if locationManager.authorizationStatus == CLAuthorizationStatus.denied
            || locationManager.authorizationStatus == CLAuthorizationStatus.restricted || locationManager.accuracyAuthorization != .fullAccuracy {
            gpsStatus = .declined
        }
        gpsStatus = .accepted
    }

    private func requestCMSensorRecorder() {
        let status = CMSensorRecorder.authorizationStatus()
        if status == .notDetermined {
            let activityManager = CMMotionActivityManager()
            let timer = Timer.scheduledTimer(withTimeInterval: 0.3, repeats: true) { timer in
                let status = CMSensorRecorder.authorizationStatus()
                if status == .authorized {
                    self.cmSensorStatus = .accepted
                    timer.invalidate()
                } else if status == .restricted || status == .denied {
                    self.cmSensorStatus = .declined
                    timer.invalidate()
                }
            }
            activityManager.startActivityUpdates(to: OperationQueue.main) { _ in
                self.cmSensorStatus = .accepted
                timer.invalidate()
                activityManager.stopActivityUpdates()
            }
        } else if status == .authorized {
            cmSensorStatus = .accepted
        }
    }

    private func checkBluetoothAuthorization(always: Bool = true) -> PermissionStatus {
        if CBManager.authorization == CBManagerAuthorization.notDetermined {
            return .requesting
        } else if CBManager.authorization == CBManagerAuthorization.denied {
            return .declined
        } else {
            return .accepted
        }
    }
    
    private func checkPushNotificationAuthorization() {
        let notificationCenter = UNUserNotificationCenter.current()
        notificationCenter.getNotificationSettings { [weak self] settings in
            if let self {
                if settings.authorizationStatus == .authorized || settings.authorizationStatus == .provisional {
                    self.notificationStatus = .accepted
                } else if settings.authorizationStatus == .denied {
                    self.notificationStatus = .declined
                } else {
                    notificationCenter.requestAuthorization(options: [.alert, .sound, .badge]) { granted, error in
                        if let error {
                            print(error)
                            self.notificationStatus = .declined
                        } else {
                            self.notificationStatus = granted ? .accepted : .declined
                        }
                    }
                }
            }
        }
    }
    
    private func checkAcceptedPerms() {
        permissionsGranted = (locationManager.authorizationStatus == CLAuthorizationStatus.authorizedAlways)

    }
    
    private func requestPermissionCamera() {
        AVCaptureDevice.requestAccess(for: .video, completionHandler: { accessGranted in
            DispatchQueue.main.async {
                self.cameraPermissionGranted = accessGranted
            }
        })
    }
    
    private func setPermisssionValues(observationPermissions: Set<String> = []) {
        gpsStatus = observationPermissions.contains("gpsAlways") ? .requesting : .non
        cameraStatus = observationPermissions.contains("camera") ? .requesting : .non
        cmSensorStatus = observationPermissions.contains("cmsensorrecorder") ? .requesting : .non
        bluetoothStatus = observationPermissions.contains("bluetoothAlways") ?.requesting : .non
    }

    func requestPermission(permissionRequest: Bool = false) {
        if permissionRequest {
            permissionsRequested = true
        }
        if permissionsRequested, let observer {
            if notificationStatus != .accepted {
                checkPushNotificationAuthorization()
            } else if gpsStatus == .requesting && gpsStatus != .accepted {
                requestGpsAuthorization()
            } else if bluetoothStatus == .requesting && bluetoothStatus != .accepted {
                bluetoothStatus = checkBluetoothAuthorization()
            } else if cmSensorStatus == .requesting && cmSensorStatus != .accepted {
                requestCMSensorRecorder()
            } else {
                observer.accepted()
            }
        }
    }
    func reset() {
        notificationStatus = .non
        gpsStatus = .non
        bluetoothStatus = .non
        cmSensorStatus = .non
        cameraStatus = .non
        permissionsRequested = false
    }
}

extension PermissionManager: CLLocationManagerDelegate {
    func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        if manager.authorizationStatus == .restricted || manager.authorizationStatus == .denied || manager.accuracyAuthorization != .fullAccuracy {
            gpsStatus = .declined
        } else if manager.authorizationStatus == .authorizedAlways || manager.authorizationStatus == .authorizedWhenInUse {
            gpsStatus = .accepted
        }
    }
}
