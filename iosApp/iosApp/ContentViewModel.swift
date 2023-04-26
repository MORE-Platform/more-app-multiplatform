//
//  ContentViewModel.swift
//  iosApp
//
//  Created by Jan Cortiel on 08.02.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import Foundation
import shared
import BackgroundTasks

class ContentViewModel: ObservableObject {
    private let userDefaults = UserDefaultsRepository()
    
    private let registrationService: RegistrationService
    private let credentialRepository: CredentialRepository
    private lazy var scheduleRepository = ScheduleRepository()
    
    private lazy var taskSchedulingService = TaskScheduleService()
    
    @Published var hasCredentials = false
    @Published var loginViewScreenNr = 0
    
    let observationFactory = IOSObservationFactory()
    
    lazy var loginViewModel: LoginViewModel = {
        let viewModel = LoginViewModel(registrationService: registrationService)
        viewModel.delegate = self
        return viewModel
    }()
    lazy var consentViewModel: ConsentViewModel = {
        let viewModel = ConsentViewModel(registrationService: registrationService)
        viewModel.delegate = self
        return viewModel
    }()
    
    lazy var dashboardViewModel: DashboardViewModel = DashboardViewModel(scheduleViewModel: ScheduleViewModel(observationFactory: observationFactory, scheduleListType: .all))
    lazy var settingsViewModel: SettingsViewModel = {
        let viewModel = SettingsViewModel()
        viewModel.delegate = self
        return viewModel
    }()
    lazy var notificationFilterViewModel: NotificationFilterViewModel = {
        let viewModel = NotificationFilterViewModel()
        viewModel.delegate = self
        return viewModel
    }()

    

    init() {
        registrationService = RegistrationService(sharedStorageRepository: userDefaults)
        credentialRepository = CredentialRepository(sharedStorageRepository: userDefaults)

        hasCredentials = credentialRepository.hasCredentials()
        
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
            self.consentViewModel.onAppear()
        }
    }
    
    func updateSchedules() {
        taskSchedulingService.startUpdateTimer()
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
        DispatchQueue.main.async { [weak self] in
            if let self {
                self.hasCredentials = true
            }
        }
        FCMService.getNotificationToken()
    }

    func credentialsDeleted() {
        DispatchQueue.main.async { [weak self] in
            if let self {
                self.hasCredentials = false
            }
        }
        showLoginView()
    }
}

extension ContentViewModel: NotificationFilterObserver {
    func onFilterChanged(filter: String, list: [String], stringTable: String) -> [String] {
        var selectedValueList = list
        if filter == String(describing: NotificationFilterTypeModel.all) {
            selectedValueList.removeAll()
        } else if selectedValueList.contains(filter) {
            selectedValueList.remove(at: selectedValueList.firstIndex(of: filter)!)
        } else {
            selectedValueList.append(filter)
        }
        notificationFilterViewModel.processFilterChange(filter: filter)
        updateNotificationFilterText(stringTable: stringTable)
        return selectedValueList
    }

    func updateNotificationFilterText(stringTable: String) {
        //TODO
    }
}
