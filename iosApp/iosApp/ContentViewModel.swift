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
    
    lazy var infoViewModel = InfoViewModel()

    lazy var bluetoothViewModel: BluetoothConnectionViewModel = {
        BluetoothConnectionViewModel(bluetoothConnector: IOSBluetoothConnector())
    }()

    
    init() {
        hasCredentials = AppDelegate.shared.credentialRepository.hasCredentials()
        
        if hasCredentials {
            DispatchQueue.main.async {
                BluetoothDeviceRepository(bluetoothConnector: IOSBluetoothConnector()).updateConnectedDevices(listenForTimeInMillis: 5000)
            }
            AppDelegate.shared.activateObservationWatcher()
        }
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
    
    private func reinitAllViewModels() {
        isLeaveStudyOpen = false
        isLeaveStudyConfirmOpen = false
        dashboardViewModel = DashboardViewModel(scheduleViewModel: ScheduleViewModel(scheduleListType: .manuals))
        runningViewModel = ScheduleViewModel(scheduleListType: .running)
        completedViewModel = ScheduleViewModel(scheduleListType: .completed)
        
        settingsViewModel = SettingsViewModel()
        settingsViewModel.delegate = self
        
        bluetoothViewModel = BluetoothConnectionViewModel(bluetoothConnector: IOSBluetoothConnector())
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
                BluetoothDeviceRepository(bluetoothConnector: IOSBluetoothConnector())
                    .updateConnectedDevices(listenForTimeInMillis: 5000)
                AppDelegate.shared.activateObservationWatcher()
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
