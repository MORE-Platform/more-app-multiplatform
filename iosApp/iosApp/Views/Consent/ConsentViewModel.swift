//
//  ConsentViewModel.swift
//  iosApp
//
//  Created by Jan Cortiel on 08.02.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
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
    var stringTable = "SettingsView"
    
    @Published private(set) var permissionModel: PermissionModel = PermissionModel(studyTitle: "Title", studyParticipantInfo: "Info", studyConsentInfo: String.localizedString(forKey: "study_consent", inTable: "SettingView", withComment: "Consent of the study"), consentInfo: [])
    @Published var isLoading = false
    @Published var error: String = ""
    @Published var showErrorAlert: Bool = false
    @Published var requestedPermissions = false

    lazy var permissionManager = PermissionManager()
    var permissionGranted = false
    
    init(registrationService: RegistrationService) {
        print("ConsentViewModel allocated!")
        coreModel = CorePermissionViewModel(registrationService: registrationService, studyConsentTitle: String.localizedString(forKey: "study_consent", inTable: stringTable, withComment: "Consent of the study"))
        super.init()
        coreModel.onConsentModelChange { model in
            self.permissionModel = model
        }
        coreModel.onLoadingChange { loading in
            if let loading = loading as? Bool {
                self.isLoading = loading
            }
        }
    }
    
    func onAppear() {
        permissionManager.observer = self
    }

    func onDisappear() {
        permissionManager.observer = nil
    }

    func requestPermissions() {
        self.requestedPermissions = true
        permissionManager.requestPermission(permissionRequest: true)
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
    
    deinit {
        print("ConsentViewModel deallocated!")
    }
}

extension ConsentViewModel: PermissionManagerObserver {
    func accepted() {
        DispatchQueue.main.async {
            self.acceptConsent()
            self.requestedPermissions = false            
        }
    }

    func declined() {
        DispatchQueue.main.async {
            self.showErrorAlert = true
            self.requestedPermissions = false
        }
    }
}
