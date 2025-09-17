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

    lazy var simpleQuestionVM = SimpleQuestionObservationViewModel()
    lazy var limeSurveyVM = LimeSurveyViewModel()

    let manualSchedule = ScheduleViewModel(scheduleListType: .manuals)
    lazy var runningViewModel = ScheduleViewModel(scheduleListType: .running)
    lazy var completedViewModel = ScheduleViewModel(scheduleListType: .completed)
    lazy var settingsViewModel: SettingsViewModel = SettingsViewModel()
    
    let coreNotificationFilterViewModel = CoreNotificationFilterViewModel()

    lazy var infoViewModel = InfoViewModel()
    
    private var cancellables = Set<AnyCancellable>()

    init() {
        
        createPublisher(for: AppDelegate.shared.credentialRepository.credentials)
            .receive(on: DispatchQueue.main)
            .sink(receiveCompletion: { _ in }) { [weak self] credentials in
                self?.hasCredentials = credentials != nil
            }
            .store(in: &cancellables)
        
        createPublisher(for: AppDelegate.shared.credentialRepository.credentialsLoaded)
            .map { $0.boolValue }
            .first(where: {$0 == true})
            .receive(on: DispatchQueue.main)
            .sink { completion in
                print("Credentials have loaded with completion: \(completion)")
            } receiveValue: { [weak self] loaded in
                self?.credentialsLoaded = loaded
            }
            .store(in: &cancellables)

        
        
        createPublisher(for: ViewManager.shared.studyIsUpdating)
            .receive(on: DispatchQueue.main)
            .sink(receiveCompletion: { _ in}) { updating in
                AppDelegate.navigationScreenHandler.studyIsUpdating(updating.boolValue)
            }
            .store(in: &cancellables)
        
        createPublisher(for: ViewManager.shared.showBluetoothView)
            .receive(on: DispatchQueue.main)
            .sink(receiveCompletion: { _ in }) { [weak self] show in
                self?.showBleView = show.boolValue
            }
            .store(in: &cancellables)
        
        createPublisher(for: AlertController.shared.alertDialogModel)
            .receive(on: DispatchQueue.main)
            .sink(receiveCompletion: {_ in }) { [weak self] alertDialogModel in
                self?.alertDialogModel = alertDialogModel
            }
            .store(in: &cancellables)
        
  

        AppDelegate.shared.unreadNotificationCountAsClosure { [weak self] kInt in
            self?.unreadNotificationCount = kInt.intValue
        }
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
        runningViewModel = ScheduleViewModel(scheduleListType: .running)
        completedViewModel = ScheduleViewModel(scheduleListType: .completed)

        settingsViewModel = SettingsViewModel()

        infoViewModel = InfoViewModel()
    }
}

