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
import KMPNativeCoroutinesCombine
import Combine

class ContentViewModel: ObservableObject {
    @Published var hasCredentials = false
    @Published var credentialsLoaded = false
    @Published var isLeaveStudyOpen: Bool = false
    @Published var isLeaveStudyConfirmOpen: Bool = false
    @Published var showBleView = false

    @Published var mainTabViewSelection = 0

    
    @Published var alertDialogModel: AlertDialogModel? = nil
    @Published var unreadNotificationCount: Int = 0

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
    
    private var cancellables = Set<AnyCancellable>()

    init() {
        createPublisher(for: AppDelegate.shared.credentialRepository.credentials)
            .sink(receiveCompletion: { _ in }) { [weak self] credentials in
                self?.hasCredentials = credentials != nil
            }
            .store(in: &cancellables)
        
        createPublisher(for: AppDelegate.shared.credentialRepository.credentialsLoaded)
            .map { $0.boolValue }
            .first(where: {$0 == true})
            .sink { completion in
                print("Credentials have loaded with completion: \(completion)")
            } receiveValue: { [weak self] loaded in
                self?.credentialsLoaded = loaded
            }
            .store(in: &cancellables)

        let coreNotificationFilterViewModel = CoreNotificationFilterViewModel()
        notificationViewModel = NotificationViewModel(filterViewModel: coreNotificationFilterViewModel)
        notificationFilterViewModel = NotificationFilterViewModel(coreViewModel: coreNotificationFilterViewModel)
        
        createPublisher(for: ViewManager.shared.studyIsUpdating)
            .sink(receiveCompletion: { _ in}) { updating in
                AppDelegate.navigationScreenHandler.studyIsUpdating(updating.boolValue)
            }
            .store(in: &cancellables)
        
        createPublisher(for: ViewManager.shared.showBluetoothView)
            .sink(receiveCompletion: { _ in }) { [weak self] show in
                self?.showBleView = show.boolValue
            }
            .store(in: &cancellables)

        AppDelegate.shared.onStudyStateChange { [weak self] studyState in
            AppDelegate.navigationScreenHandler.setStudyState(studyState)
        }
        
        createPublisher(for: AlertController.shared.alertDialogModel)
            .sink(receiveCompletion: {_ in }) { [weak self] alertDialogModel in
                self?.alertDialogModel = alertDialogModel
            }
            .store(in: &cancellables)
        
  

        AppDelegate.shared.unreadNotificationCountAsClosure { [weak self] kInt in
            self?.unreadNotificationCount = kInt.intValue
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

