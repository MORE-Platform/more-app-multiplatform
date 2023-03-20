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
    
    @Published var navigationTitle = ""

    let loginViewModel: LoginViewModel
    let consentViewModel: ConsentViewModel
    let dashboardViewModel: DashboardViewModel
    let permissionManager: PermissionManager
    let settingsViewModel: SettingsViewModel

    @Published private(set) var permissionModel: PermissionModel = PermissionModel(studyTitle: "", studyParticipantInfo: "", consentInfo: [])

    init() {
        registrationService = RegistrationService(sharedStorageRepository: userDefaults)
        credentialRepository = CredentialRepository(sharedStorageRepository: userDefaults)
        hasCredentials = credentialRepository.hasCredentials()
        
        loginViewModel = LoginViewModel(registrationService: registrationService)
        dashboardViewModel = DashboardViewModel()
        permissionManager = PermissionManager.permObj
        consentViewModel = ConsentViewModel(registrationService: registrationService)
        settingsViewModel = SettingsViewModel()

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

    func loadData() {
        self.dashboardViewModel.loadStudy()
        self.dashboardViewModel.scheduleViewModel.loadData()
    }
}

extension ContentViewModel: LoginViewModelListener {
    func tokenValid(study: Study) {
        self.consentViewModel.consentInfo = study.consentInfo
        self.consentViewModel.buildConsentModel()
        DispatchQueue.main.async {
            self.permissionModel = self.consentViewModel.permissionModel
        }
        showConsentView()
    }
}

extension ContentViewModel: ConsentViewModelListener {
    func decline() {
        showLoginView()
    }
    
    func credentialsStored() {
        self.loadData()
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
