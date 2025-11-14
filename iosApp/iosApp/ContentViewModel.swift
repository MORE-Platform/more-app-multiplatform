//
//  ContentViewModel.swift
//  iosApp
//
//  Created by Jan Cortiel on 08.02.23.
//  Copyright © 2023 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung 
//  Licensed under the Apache 2.0 license with Commons Clause 
//  (see https://www.apache.org/licenses/LICENSE-2.0 and
//  https://commonsclause.com/).
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
    
    @Published var mainTabViewSelection = 0
    
    @Published var finishText: String? = nil
    @Published var alertDialogModel: AlertDialogModel? = nil
    @Published var unreadNotificationCount: Int = 0

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
    
    lazy var taskDetailsVM: TaskDetailsViewModel = {
        TaskDetailsViewModel(dataRecorder: AppDelegate.shared.dataRecorder)
    }()
    
    lazy var simpleQuestionVM = SimpleQuestionObservationViewModel()
    lazy var limeSurveyVM = LimeSurveyViewModel()
    
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
        
        ViewManager.shared.studyIsUpdatingAsClosure { kBool in
            AppDelegate.navigationScreenHandler.studyIsUpdating(kBool.boolValue)
        }
        
        ViewManager.shared.showBluetoothViewAsClosure { [weak self] kBool in
            if kBool.boolValue {
                self?.showBleView = kBool.boolValue
            }
        }
        
        AppDelegate.shared.onStudyStateChange { [weak self] studyState in
            self?.finishText = AppDelegate.shared.finishText
            AppDelegate.navigationScreenHandler.setStudyState(studyState)
        }
        
        AlertController.shared.onNewAlertDialogModel { [weak self] alertDialogModel in
            self?.alertDialogModel = alertDialogModel
        }
        
        AppDelegate.shared.unreadNotificationCountAsClosure { [weak self] kInt in
            self?.unreadNotificationCount = kInt.intValue
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
    
    func getTaskDetailsVM(navigationState: NavigationState) -> TaskDetailsViewModel {
        if let scheduleId = navigationState.scheduleId {
            taskDetailsVM.setSchedule(scheduleId: scheduleId)
        }
        return taskDetailsVM
    }
    
    func getSimpleQuestionObservationVM(navigationState: NavigationState) -> SimpleQuestionObservationViewModel {
        simpleQuestionVM.setScheduleId(navigationState: navigationState)
        return simpleQuestionVM
    }
    
    func getLimeSurveyVM(navigationModalState: NavigationModalState) -> LimeSurveyViewModel {
        limeSurveyVM.setNavigationModalState(navigationModalState: navigationModalState)
        return limeSurveyVM
    }

    
    private func reinitAllViewModels() {
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
        DispatchQueue.main.async {
            self.consentViewModel.consentInfo = study.consentInfo
            self.consentViewModel.buildConsentModel()
            self.showConsentView()
        }
    }
}

extension ContentViewModel: ConsentViewModelListener {
    func decline() {
        showLoginView()
    }
    
    func credentialsStored() {
        reinitAllViewModels()
        DispatchQueue.main.async { [weak self] in
            self?.hasCredentials = true
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
