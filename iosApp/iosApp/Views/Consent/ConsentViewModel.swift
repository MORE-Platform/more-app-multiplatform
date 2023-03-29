//
//  ConsentViewModel.swift
//  iosApp
//
//  Created by Jan Cortiel on 08.02.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import shared
import UIKit
import AVFoundation

protocol ConsentViewModelListener {
    func credentialsStored()
    func decline()
    func credentialsDeleted()
}

class ConsentViewModel: NSObject, ObservableObject {
    private let coreModel: CorePermissionViewModel
    var consentInfo: String? = nil
    var delegate: ConsentViewModelListener? = nil
    
    @Published private(set) var permissionModel: PermissionModel = PermissionModel(studyTitle: "Title", studyParticipantInfo: "Info", studyConsentInfo: "Study Consent", consentInfo: [])
    @Published private(set) var isLoading = false
    @Published var error: String = ""
    @Published var showErrorAlert: Bool = false

    var permissionManager = PermissionManager()
    var permissionGranted = false
    
    init(registrationService: RegistrationService) {
        coreModel = CorePermissionViewModel(registrationService: registrationService)
        super.init()
        
        permissionManager.observer = self
        
        coreModel.onConsentModelChange { model in
            self.permissionModel = model
        }
        coreModel.onLoadingChange { loading in
            if let loading = loading as? Bool {
                self.isLoading = loading
            }
        }
    }
    
    func requestPermissions() {
        permissionManager.requestPermission()
    }

    func reloadPermissions() {
        coreModel.onConsentModelChange { model in
            self.permissionModel = model
        }
    }
    
    private func acceptConsent() {
        if let consentInfo, let uniqueId = UIDevice.current.identifierForVendor?.uuidString {
            coreModel.acceptConsent(consentInfoMd5: consentInfo.toMD5(), uniqueDeviceId: uniqueId) { credentialsStored in
                DispatchQueue.main.async {
                    self.delegate?.credentialsStored()
                }
            } onError: { error in
                if let error {
                    DispatchQueue.main.async {
                        self.error = error.message
                    }
                }
            }
        }
    }
    
    func buildConsentModel() {
        coreModel.buildConsentModel()
    }
    
    func decline() {
        delegate?.decline()
    }
}

extension ConsentViewModel: PermissionManagerObserver {
    func accepted() {
        self.acceptConsent()
    }

    func declined() {
        self.showErrorAlert = true
    }
}
