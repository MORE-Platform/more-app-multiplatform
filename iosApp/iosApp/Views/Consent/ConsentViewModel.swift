//
//  ConsentViewModel.swift
//  iosApp
//
//  Created by Jan Cortiel on 08.02.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import shared
import UIKit

protocol ConsentViewModelListener {
    func credentialsStored()
    func decline()
    func credentialsDeleted()
}

class ConsentViewModel: ObservableObject {
    private let coreModel: CorePermissionViewModel
    var consentInfo: String? = nil
    var delegate: ConsentViewModelListener? = nil
    
    @Published private(set) var permissionModel: PermissionModel = PermissionModel(studyTitle: "Title", studyParticipantInfo: "Info", consentInfo: [])
    @Published private(set) var isLoading = false
    @Published var error: String = ""
    @Published var showErrorAlert: Bool = false
    
    
    init(registrationService: RegistrationService) {
        coreModel = CorePermissionViewModel(registrationService: registrationService)
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
    
    func buildConsentModel() {
        coreModel.buildConsentModel()
    }
    
    func decline() {
        delegate?.decline()
    }
}
