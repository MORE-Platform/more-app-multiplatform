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
    
    lazy var dashboardFilterViewModel: DashboardFilterViewModel = {
        let viewModel = DashboardFilterViewModel()
        viewModel.delegate = self
        return viewModel
    }()
    lazy var dashboardViewModel: DashboardViewModel = DashboardViewModel(dashboardFilterViewModel: dashboardFilterViewModel, scheduleViewModel: ScheduleViewModel(observationFactory: observationFactory, dashboardFilterViewModel: dashboardFilterViewModel, scheduleListType: .all))
    lazy var settingsViewModel: SettingsViewModel = {
        let viewModel = SettingsViewModel()
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

extension ContentViewModel: DashboardFilterObserver {
    func onFilterChanged(multiSelect: Bool, filter: String, list: [String], stringTable: String) -> [String] {
        var selectedValueList = list
        if multiSelect {
            if filter == String.localizedString(forKey: "All Items", inTable: stringTable, withComment: "String for All Items") {
                dashboardFilterViewModel.observationTypeFilter.removeAll()
            } else {
                if dashboardFilterViewModel.observationTypeFilter.contains(filter) {
                    dashboardFilterViewModel.observationTypeFilter.remove(at: dashboardFilterViewModel.observationTypeFilter.firstIndex(of: filter)!)
                } else {
                    dashboardFilterViewModel.observationTypeFilter.append(filter)
                }
            }
            selectedValueList = dashboardFilterViewModel.observationTypeFilter
            dashboardFilterViewModel.setObservationTypeFilters()
        } else {
            if !selectedValueList.isEmpty {
                selectedValueList.removeAll()
            }
            selectedValueList.append(filter)
            dashboardFilterViewModel.dateFilterString = filter
            dashboardFilterViewModel.setDateFilterValue()
        }
        updateFilterText(stringTable: stringTable)
        return selectedValueList
    }
    
    func updateFilterText(stringTable: String) {
        var typeFilterText = ""
        var dateFilterText = ""
        if noFilterSet() {
            dashboardViewModel.filterText = String.localizedString(forKey: "no_filter_activated", inTable: stringTable, withComment: "No filter set")
        } else {
            if typeFilterSet() {
                if dashboardFilterViewModel.observationTypeFilter.count == 1 {
                    typeFilterText = "\(dashboardFilterViewModel.observationTypeFilter.count) \(String.localizedString(forKey: "type", inTable: stringTable, withComment: "Observation type"))"
                } else {
                    typeFilterText = "\(dashboardFilterViewModel.observationTypeFilter.count) \(String.localizedString(forKey: "type_plural", inTable: stringTable, withComment: "Observation types"))"
                }
            }
            if dateFilterSet() {
                dateFilterText = String.localizedString(forKey: dashboardFilterViewModel.dateFilterString, inTable: stringTable, withComment: "Time filter")
            }
            if dateFilterSet() && typeFilterSet() {
                dashboardViewModel.filterText = "\(typeFilterText), \(dateFilterText)"
            } else if dateFilterSet(){
                dashboardViewModel.filterText = dateFilterText
            } else {
                dashboardViewModel.filterText = typeFilterText
            }
        }
    }
    
    func dateFilterSet() -> Bool {
        return dashboardFilterViewModel.dateFilterString != "ENTIRE_TIME"
    }
    
    func typeFilterSet() -> Bool {
        return !dashboardFilterViewModel.observationTypeFilter.isEmpty
    }
    
    func noFilterSet() -> Bool {
        return !dateFilterSet() && !typeFilterSet()
    }
    
}
