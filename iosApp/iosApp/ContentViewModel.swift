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
    
    @Published var hasCredentials = false {
        didSet {
            if hasCredentials {
                loadData()
            }
        }
    }
    @Published var loginViewScreenNr = 0
    
    @Published var navigationTitle = ""

    let loginViewModel: LoginViewModel
    let consentViewModel: ConsentViewModel
    let dashboardViewModel: DashboardViewModel
    let dashboardFilterViewModel: DashboardFilterViewModel
    let settingsViewModel: SettingsViewModel

    init() {
        registrationService = RegistrationService(sharedStorageRepository: userDefaults)
        credentialRepository = CredentialRepository(sharedStorageRepository: userDefaults)
        
        loginViewModel = LoginViewModel(registrationService: registrationService)
        dashboardViewModel = DashboardViewModel()
        dashboardFilterViewModel = DashboardFilterViewModel()
        consentViewModel = ConsentViewModel(registrationService: registrationService)
        settingsViewModel = SettingsViewModel()
        hasCredentials = credentialRepository.hasCredentials()

        loginViewModel.delegate = self
        consentViewModel.delegate = self
        settingsViewModel.delegate = self
    }
    
    func showLoginView() {
        DispatchQueue.main.async {
            self.registrationService.reset()
            self.loginViewScreenNr = 0
        }
    }
    
    func showConsentView() {
        DispatchQueue.main.async {
            self.loginViewScreenNr = 1
        }
    }

    private func loadData() {
        self.dashboardViewModel.loadData()
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

    func credentialsDeleted() {
        DispatchQueue.main.async {
            self.hasCredentials = false
        }
    }
}
