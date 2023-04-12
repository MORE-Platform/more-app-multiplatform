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

    private let locationManager: CLLocationManager = CLLocationManager()
    private var gpsAuthorizationStatus: CLAuthorizationStatus?

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
    
    private var notificationStatus: PermissionStatus = .requesting {
        didSet {
            if notificationStatus == .accepted {
                Task { @MainActor in
                    UIApplication.shared.registerForRemoteNotifications()
                }
                requestPermission()
            } else if notificationStatus == .declined {
                observer?.declined()
            }
        }
    }

    private var cameraNeeded: Bool = false

    private var bluetoothStatus: PermissionStatus = .requesting {
        didSet {
            if bluetoothStatus == .accepted {
                requestPermission()
            } else if bluetoothStatus == .declined {
                observer?.declined()
            }
        }
    }
    private var bluetoothNeeded: Bool = false

    var permissionsGranted: Bool = true

    override init() {
        super.init()
        gpsAuthorizationStatus = locationManager.authorizationStatus
        locationManager.delegate = self
        locationManager.desiredAccuracy = kCLLocationAccuracyBest

        setPermisssionValues(observationPermissions: IOSObservationFactory().sensorPermissions())
    }

    private func requestGpsAuthorization(always: Bool = true) -> PermissionStatus {
        if locationManager.authorizationStatus == .notDetermined {
            if always {
                locationManager.requestAlwaysAuthorization()
            } else {
                locationManager.requestWhenInUseAuthorization()
            }
            return .requesting
        } else if locationManager.authorizationStatus == CLAuthorizationStatus.denied
            || locationManager.authorizationStatus == CLAuthorizationStatus.restricted || locationManager.accuracyAuthorization != .fullAccuracy {
            observer?.declined()
            return .declined
        }
        return .accepted
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
        if cameraNeeded {
            permissionsGranted = (locationManager.authorizationStatus == CLAuthorizationStatus.authorizedAlways)
        }
    }
    
    private func requestPermissionCamera() {
        AVCaptureDevice.requestAccess(for: .video, completionHandler: { accessGranted in
            DispatchQueue.main.async {
                self.cameraPermissionGranted = accessGranted
            }
        })
    }
    
    private func setPermisssionValues(observationPermissions: Set<String> = []) {
        gpsNeeded = observationPermissions.contains("gpsAlways")
        cameraNeeded = observationPermissions.contains("camera")
        cmSensorRecorderNeeded = observationPermissions.contains("cmsensorrecorder")
        bluetoothNeeded = observationPermissions.contains("bluetoothAlways")
    }

    func requestPermission() {
        if let observer {
            if notificationStatus != .accepted {
                checkPushNotificationAuthorization()
            } else if gpsNeeded && gpsStatus != .accepted {
                gpsStatus = requestGpsAuthorization()
            } else if bluetoothNeeded && bluetoothStatus != .accepted {
                bluetoothStatus = checkBluetoothAuthorization()
            } else if cmSensorRecorderNeeded && cmSensorStatus != .accepted {
                requestCMSensorRecorder()
            } else {
                observer.accepted()
            }
        }
    }

}

extension PermissionManager: CLLocationManagerDelegate {
    func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        if manager.authorizationStatus == .restricted || manager.authorizationStatus == .denied || manager.accuracyAuthorization != .fullAccuracy {
            observer?.declined()
        } else if manager.authorizationStatus == .authorizedAlways || manager.authorizationStatus == .authorizedWhenInUse {
            gpsStatus = .accepted
            requestPermission()
        }
    }
}
