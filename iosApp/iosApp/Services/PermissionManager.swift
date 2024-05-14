//
//  PermissionManager.swift
//  iosApp
//
//  Created by Daniil Barkov on 06.03.23.
//  Copyright © 2023 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung
//  Licensed under the Apache 2.0 license with Commons Clause
//  (see https://www.apache.org/licenses/LICENSE-2.0 and
//  https://commonsclause.com/).
//

import AVFoundation
import CoreBluetooth
import CoreLocation
import CoreMotion
import Foundation
import shared
import UIKit
import UserNotifications

enum PermissionStatus {
    case accepted, declined, requesting, non
}

extension PermissionStatus {
    func userResponded() -> Bool {
        self == .accepted || self == .declined
    }

    func resetStatus() -> PermissionStatus {
        userResponded() || self == .requesting ? .requesting : .non
    }

    func declined() -> Bool {
        self == .declined
    }
}

protocol PermissionManagerObserver {
    func accepted()
}

class PermissionManager: NSObject, ObservableObject {
    var observer: PermissionManagerObserver?
    private var permissionsRequested = false

    private var authorizationTimer: Timer?

    private let locationManager: CLLocationManager = CLLocationManager()
    private var cbManager: CBCentralManager?

    private var cameraPermissionGranted = false

    private var gpsStatus: PermissionStatus = .non {
        didSet {
            if gpsStatus.userResponded() {
                locationManager.delegate = nil
                requestPermission()
            } else if gpsStatus == .requesting {
                locationManager.delegate = self
                locationManager.desiredAccuracy = kCLLocationAccuracyBest
            }
        }
    }

    private var cmSensorStatus: PermissionStatus = .non {
        didSet {
            print("cmSensor \(cmSensorStatus)")
            if cmSensorStatus.userResponded() {
                requestPermission()
            }
        }
    }

    private var notificationStatus: PermissionStatus = .requesting {
        didSet {
            if notificationStatus.userResponded() {
                if notificationStatus == .accepted {
                    AppDelegate.registerForNotifications()
                } else {
                    AlertController.shared.openAlertDialog(model: AlertDialogModel(title: "Notification Permissions Not Granted", message: "We request permission to send you push notifications. This assists in maintaining the study's current status at all times and serves as a reminder for your tasks.", positiveTitle: "Proceed to Settings", negativeTitle: "Proceed Without Granting Permissions", onPositive: {
                        if let url = URL(string: UIApplication.openSettingsURLString), UIApplication.shared.canOpenURL(url) {
                            UIApplication.shared.open(url, options: [:], completionHandler: nil)
                        }
                        AlertController.shared.closeAlertDialog()
                    }, onNegative: {
                        AlertController.shared.closeAlertDialog()
                    }))
                }
                requestPermission()
            }
        }
    }

    private var cameraStatus: PermissionStatus = .non {
        didSet {
            if cameraStatus.userResponded() {
                requestPermission()
            }
        }
    }

    private var bluetoothStatus: PermissionStatus = .non {
        didSet {
            if bluetoothStatus.userResponded() {
                requestPermission()
            }
        }
    }

    var permissionsGranted: Bool = true

    override init() {
        super.init()
        setPermissionValues(observationPermissions: AppDelegate.shared.observationFactory.studySensorPermissions())
    }

    private func requestGpsAuthorization(always: Bool = true) {
        if locationManager.authorizationStatus == .notDetermined {
            locationManager.requestWhenInUseAuthorization()
            locationManager.requestAlwaysAuthorization()
            gpsStatus = .requesting
        } else if locationManager.authorizationStatus == CLAuthorizationStatus.denied
            || locationManager.authorizationStatus == CLAuthorizationStatus.restricted || locationManager.accuracyAuthorization != .fullAccuracy {
            gpsStatus = .declined
        } else {
            gpsStatus = .accepted
        }
    }

    private func requestCMSensorRecorder() {
        let status = CMSensorRecorder.authorizationStatus()
        if status == .notDetermined {
            DispatchQueue.main.async { [weak self] in
                let activityManager = CMMotionActivityManager()
                self?.authorizationTimer = Timer.scheduledTimer(withTimeInterval: 0.3, repeats: true) { [weak self] timer in
                    guard let self = self else {
                        timer.invalidate()
                        return
                    }
                    let status = CMSensorRecorder.authorizationStatus()
                    if status == .authorized {
                        self.cmSensorStatus = .accepted
                        timer.invalidate()
                        self.authorizationTimer = nil
                    } else if status == .denied || status == .restricted {
                        self.cmSensorStatus = .declined
                        timer.invalidate()
                        self.authorizationTimer = nil
                    }
                }

                activityManager.startActivityUpdates(to: OperationQueue.main) { [weak self] _ in
                    print("Starting Activity Updates")
                    self?.cmSensorStatus = .accepted
                    self?.authorizationTimer?.invalidate()
                    self?.authorizationTimer = nil
                    activityManager.stopActivityUpdates()
                }
            }
        } else if status == .authorized {
            cmSensorStatus = .accepted
        } else {
            cmSensorStatus = .declined
        }
    }

    private func checkBluetoothAuthorization(always: Bool = true) -> PermissionStatus {
        print("Checking for Bluetooth authorization")
        if CBManager.authorization == .notDetermined {
            cbManager = CBCentralManager(delegate: self, queue: nil)
            return .requesting
        } else if CBManager.authorization == .restricted || CBManager.authorization == .denied {
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
                    DispatchQueue.main.async {
                        UIApplication.shared.registerForRemoteNotifications()
                    }
                } else if settings.authorizationStatus == .denied {
                    self.notificationStatus = .declined
                } else {
                    notificationCenter.requestAuthorization(options: [.alert, .sound, .badge]) { granted, error in
                        if let error {
                            print(error)
                            self.notificationStatus = .declined
                        } else {
                            self.notificationStatus = granted ? .accepted : .declined
                            if granted {
                                DispatchQueue.main.async {
                                    UIApplication.shared.registerForRemoteNotifications()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private func requestPermissionCamera() {
        AVCaptureDevice.requestAccess(for: .video, completionHandler: { accessGranted in
            DispatchQueue.main.async {
                self.cameraPermissionGranted = accessGranted
            }
        })
    }

    func anyNeededPermissionDeclined() -> Bool {
        gpsStatus.declined() || cameraStatus.declined() || cmSensorStatus.declined() || bluetoothStatus.declined()
    }

    func setPermissionValues(observationPermissions: Set<String> = []) {
        gpsStatus = observationPermissions.contains("gpsAlways") ? .requesting : .non
        cameraStatus = observationPermissions.contains("camera") ? .requesting : .non
        cmSensorStatus = observationPermissions.contains("cmsensorrecorder") ? .requesting : .non
        bluetoothStatus = observationPermissions.contains("bluetoothAlways") ? .requesting : .non
    }

    func requestPermission(permissionRequest: Bool = false) {
        print("Requesting Permissions")
        if permissionRequest {
            permissionsRequested = true
        }
        if permissionsRequested, let observer {
            if notificationStatus == .requesting && !notificationStatus.userResponded() {
                checkPushNotificationAuthorization()
            } else if gpsStatus == .requesting && !gpsStatus.userResponded() {
                requestGpsAuthorization()
            } else if bluetoothStatus == .requesting && !bluetoothStatus.userResponded() {
                bluetoothStatus = checkBluetoothAuthorization()
            } else if cmSensorStatus == .requesting && !cmSensorStatus.userResponded() {
                requestCMSensorRecorder()
            } else {
                print("Continuing")
                observer.accepted()
            }
        }
    }

    func resetRequest() {
        notificationStatus = .requesting
        gpsStatus = gpsStatus.resetStatus()
        bluetoothStatus = bluetoothStatus.resetStatus()
        cmSensorStatus = cmSensorStatus.resetStatus()
        cameraStatus = cameraStatus.resetStatus()
        permissionsRequested = false
    }

    func reset() {
        notificationStatus = .requesting
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
    
    private static var permissionAlertOpenedThisSession = false

    static func openSensorPermissionDialog() {
        if permissionAlertOpenedThisSession {
            return
        }
        
        permissionAlertOpenedThisSession = true
        AlertController.shared.openAlertDialog(model: AlertDialogModel(title: "Required Permissions Were Not Granted", message: "This study requires one or more sensor permissions to function correctly. You may choose to decline these permissions; however, doing so may result in the application and study not functioning fully or as expected. Would you like to navigate to settings to allow the app access to these necessary permissions?", positiveTitle: "Proceed to Settings", negativeTitle: "Proceed Without Granting Permissions", onPositive: {
            if let url = URL(string: UIApplication.openSettingsURLString), UIApplication.shared.canOpenURL(url) {
                UIApplication.shared.open(url, options: [:], completionHandler: nil)
            }
            AlertController.shared.closeAlertDialog()
        }, onNegative: {
            AlertController.shared.closeAlertDialog()
        }))
    }
}

extension PermissionManager: CBCentralManagerDelegate {
    func centralManagerDidUpdateState(_ central: CBCentralManager) {
        cbManager = nil
        bluetoothStatus = checkBluetoothAuthorization()
    }
}
