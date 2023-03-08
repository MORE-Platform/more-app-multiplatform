//
//  ContentViewModel.swift
//  iosApp
//
//  Created by Jan Cortiel on 08.02.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import Foundation
import shared

class ContentViewModel: ObservableObject {
    private let userDefaults = UserDefaultsRepository()
    
    private let registrationService: RegistrationService
    private let credentialRepository: CredentialRepository
    
    @Published var hasCredentials = false
    @Published var loginViewScreenNr = 0
    
    let loginViewModel: LoginViewModel
    let consentViewModel: ConsentViewModel
    let dashboardViewModel: DashboardViewModel
    
    init() {
        registrationService = RegistrationService(sharedStorageRepository: userDefaults)
        credentialRepository = CredentialRepository(sharedStorageRepository: userDefaults)
        hasCredentials = credentialRepository.hasCredentials()
        
        loginViewModel = LoginViewModel(registrationService: registrationService)
        consentViewModel = ConsentViewModel(registrationService: registrationService)
        dashboardViewModel = DashboardViewModel()

        loginViewModel.delegate = self
        consentViewModel.delegate = self
    }
    
    func showLoginView() {
        self.registrationService.reset()
        DispatchQueue.main.async {
            self.loginViewScreenNr = 0
        }
    }
    
    func showConsentView() {
        DispatchQueue.main.async {
            self.loginViewScreenNr = 1
        }
    }
}

extension ContentViewModel: LoginViewModelListener {
    func tokenValid(study: Study) {
        self.consentViewModel.consentInfo = study.consentInfo
        self.consentViewModel.buildConsentModel()
        showConsentView()
    }
}

extension ContentViewModel: ConsentViewModelListener {
    func decline() {
        showLoginView()
    }
    
    func credentialsStored() {
        DispatchQueue.main.async {
            self.hasCredentials = true
        }

    }
}
