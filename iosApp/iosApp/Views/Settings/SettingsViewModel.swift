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

class SettingsViewModel: ObservableObject {
    private let coreSettingsViewModel: CoreSettingsViewModel
    var delegate: ConsentViewModelListener? = nil
    
    @Published var studyTitle: String?
    @Published private(set) var permissionModel: PermissionModel?
    @Published var dataDeleted = false
    @Published var showSettings = false
    
    init() {
        coreSettingsViewModel = CoreSettingsViewModel(shared: AppDelegate.shared)
        coreSettingsViewModel.onLoadStudy { [weak self] study in
            self?.studyTitle = study?.studyTitle
        }
        coreSettingsViewModel.onPermissionChange { [weak self] permissions in
            self?.permissionModel = permissions
        }
    }
    
    func leaveStudy() {
        dataDeleted = true
        coreSettingsViewModel.exitStudy()
        self.delegate?.credentialsDeleted()
    }
    
    func viewDidAppear() {
        coreSettingsViewModel.viewDidAppear()
    }
    
    func viewDidDisappear() {
        coreSettingsViewModel.viewDidDisappear()
    }
}
