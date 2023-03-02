//
//  ConsentViewModel.swift
//  iosApp
//
//  Created by Jan Cortiel on 08.02.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import shared
import UIKit
import CoreLocation

protocol ConsentViewModelListener {
    func credentialsStored()
    func decline()
}

class ConsentViewModel: NSObject, ObservableObject, CLLocationManagerDelegate {
    private let coreModel: CorePermissionViewModel
    var consentInfo: String? = nil
    var delegate: ConsentViewModelListener? = nil
    
    @Published private(set) var permissionModel: PermissionModel = PermissionModel(studyTitle: "Title", studyParticipantInfo: "Info", consentInfo: [])
    @Published private(set) var isLoading = false
    @Published var error: String = ""
    @Published var showErrorAlert: Bool = false
    @Published var authorisationStatus: CLAuthorizationStatus = .notDetermined
    @Published var authorizationStatus: CLAuthorizationStatus
        
    private let locationManager: CLLocationManager
    
    
    init(registrationService: RegistrationService) {
        coreModel = CorePermissionViewModel(registrationService: registrationService)
        locationManager = CLLocationManager()
        authorizationStatus = locationManager.authorizationStatus
        
        super.init()
        
        locationManager.delegate = self
        locationManager.desiredAccuracy = kCLLocationAccuracyBest
        locationManager.startUpdatingLocation()
        coreModel.onConsentModelChange { model in
            self.permissionModel = model
        }
        coreModel.onLoadingChange{ loading in
            if let loading = loading as? Bool {
                self.isLoading = loading
            }
        }
    }
    
    func acceptConsent() {
        if let consentInfo, let uniqueId = UIDevice.current.identifierForVendor?.uuidString {
            coreModel.acceptConsent(consentInfoMd5: consentInfo.toMD5(), uniqueDeviceId: uniqueId) { credentialsStored in
                self.delegate?.credentialsStored()
            } onError: { error in
                if let error {
                    self.error = error.message
                }
            }
            
        }
    }
    
    public func requestAuthorisation(always: Bool = false) {
        if always {
            self.locationManager.requestAlwaysAuthorization()
        } else {
            self.locationManager.requestWhenInUseAuthorization()
        }
    }
    
    func requestPermission() {
        locationManager.requestWhenInUseAuthorization()
    }

    func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        authorizationStatus = manager.authorizationStatus
    }
    
    func buildConsentModel() {
        coreModel.buildConsentModel()
    }
    
    func decline() {
        delegate?.decline()
    }
}
