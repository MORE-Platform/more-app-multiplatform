//
//  SettingsViewModel.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 08.03.23.
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
import Combine
import KMPNativeCoroutinesCombine

class SettingsViewModel: ObservableObject {
    private let coreSettingsViewModel = CoreSettingsViewModel(shared: AppDelegate.shared)
    
    @Published var studyTitle: String?
    @Published var permissionModel: PermissionModel?
    @Published var showSettings = false
    
    private var cancellables: Set<AnyCancellable> = []
    init() {
        createPublisher(for: coreSettingsViewModel.study)
            .receive(on: DispatchQueue.main)
            .sink(receiveCompletion: {_ in}) { [weak self] studyEntity in
                self?.studyTitle = studyEntity?.studyTitle
            }
            .store(in: &cancellables)
        
        createPublisher(for: coreSettingsViewModel.permissionModel)
            .receive(on: DispatchQueue.main)
            .sink(receiveCompletion: {_ in}) { [weak self] permissions in
                self?.permissionModel = permissions
            }
            .store(in: &cancellables)
    
    }
    
    func leaveStudy() {
        coreSettingsViewModel.exitStudy()
    }
}
