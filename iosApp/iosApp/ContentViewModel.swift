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
    private let registrationService = RegistrationService(shared: AppDelegate.shared)
    
    @Published var hasCredentials = false
    @Published var loginViewScreenNr = 0
    @Published var isLeaveStudyOpen: Bool = false
    @Published var isLeaveStudyConfirmOpen: Bool = false
    @Published var showBleView = false
    
    @Published var studyIsUpdating: Bool = false
    @Published var currentStudyState: StudyState = .none
    
    @Published var finishText: String? = nil

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
    
    var dashboardViewModel: DashboardViewModel = DashboardViewModel(scheduleViewModel: ScheduleViewModel(scheduleListType: .manuals))
    lazy var runningViewModel = ScheduleViewModel(scheduleListType: .running)
    lazy var completedViewModel = ScheduleViewModel(scheduleListType: .completed)
    lazy var settingsViewModel: SettingsViewModel = {
        let viewModel = SettingsViewModel()
        viewModel.delegate = self
        return viewModel
    }()
    
    var notificationViewModel: NotificationViewModel
    
    var notificationFilterViewModel: NotificationFilterViewModel
    
    lazy var infoViewModel = InfoViewModel()

    lazy var bluetoothViewModel: BluetoothConnectionViewModel = BluetoothConnectionViewModel()
    
    init() {
        let coreNotificationFilterViewModel = CoreNotificationFilterViewModel()
        notificationViewModel = NotificationViewModel(filterViewModel: coreNotificationFilterViewModel)
        notificationFilterViewModel = NotificationFilterViewModel(coreViewModel: coreNotificationFilterViewModel)
        hasCredentials = AppDelegate.shared.credentialRepository.hasCredentials()
        
        AppDelegate.shared.onStudyIsUpdatingChange { [weak self] kBool in
            print("Study updating ")
            self?.studyIsUpdating = kBool.boolValue
        }
        
        AppDelegate.shared.onStudyStateChange { [weak self] studyState in
            self?.finishText = AppDelegate.shared.finishText
            self?.currentStudyState = studyState
        }
        
        if hasCredentials {
            scanBluetooth()
        }
    }
    
    func scanBluetooth() {
        let pair = AppDelegate.shared.showBleSetup()

        if let hasBleObservations = pair.second, hasBleObservations.boolValue {
            if let firstStartup = pair.first, firstStartup.boolValue {
                DispatchQueue.main.async {
                    self.showBleView = true
                }
            }
        }
    }
    
    func showLoginView() {
        DispatchQueue.main.async {
            self.registrationService.reset()
            self.loginViewScreenNr = 0
            self.hasCredentials = false
        }
    }
    
    func showConsentView() {
        DispatchQueue.main.async {
            self.loginViewScreenNr = 1
            self.consentViewModel.onAppear()
        }
    }
    
    private func reinitAllViewModels() {
        self.isLeaveStudyOpen = false
        isLeaveStudyConfirmOpen = false
        dashboardViewModel = DashboardViewModel(scheduleViewModel: ScheduleViewModel(scheduleListType: .manuals))
        runningViewModel = ScheduleViewModel(scheduleListType: .running)
        completedViewModel = ScheduleViewModel(scheduleListType: .completed)
        
        let coreNotificationFilterViewModel = CoreNotificationFilterViewModel()
        notificationViewModel = NotificationViewModel(filterViewModel: coreNotificationFilterViewModel)
        notificationFilterViewModel = NotificationFilterViewModel(coreViewModel: coreNotificationFilterViewModel)
        
        settingsViewModel = SettingsViewModel()
        settingsViewModel.delegate = self
        
        bluetoothViewModel = BluetoothConnectionViewModel()
        infoViewModel = InfoViewModel()
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
        reinitAllViewModels()
        DispatchQueue.main.async { [weak self] in
            if let self {
                self.hasCredentials = true
                self.scanBluetooth()
            }
        }
        AppDelegate.shared.doNewLogin()
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
