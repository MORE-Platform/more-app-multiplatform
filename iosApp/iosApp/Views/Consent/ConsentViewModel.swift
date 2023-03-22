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
    
    @Published private(set) var permissionModel: PermissionModel = PermissionModel(studyTitle: "Title", studyParticipantInfo: "Info", studyConsentInfo: "", consentInfo: [])
    @Published private(set) var isLoading = false
    @Published var error: String = ""
    @Published var showErrorAlert: Bool = false

    var permManager = PermissionManager.permObj
    var permissionGranted = false
    
    init(registrationService: RegistrationService) {
        coreModel = CorePermissionViewModel(registrationService: registrationService)
        
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
    
    func reloadPermissions() {
        coreModel.onConsentModelChange { model in
            self.permissionModel = model
        }
    }
    
    func acceptConsent() {
//        if(permManager.permissionsGranted){
            if let consentInfo, let uniqueId = UIDevice.current.identifierForVendor?.uuidString {
                coreModel.acceptConsent(consentInfoMd5: consentInfo.toMD5(), uniqueDeviceId: uniqueId) { credentialsStored in
                    self.delegate?.credentialsStored()
                } onError: { error in
                    if let error {
                        self.error = error.message
                    }
                }
                
            }
//        } else if(permManager.permissionsDenied){
//            showErrorAlert = true
//        }
    }
    
    func buildConsentModel() {
        coreModel.buildConsentModel()
    }
    
    func decline() {
        delegate?.decline()
    }
}
