//
//  SettingsViewModel.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 08.03.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
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
        AppDelegate.recorder.stopAll()
        AppDelegate.dataManager.stopListeningToCountChanges()
        dataDeleted = true
        coreSettingsViewModel.exitStudy()
        FCMService().deleteNotificationToken()
        self.delegate?.credentialsDeleted()
    }
    
    func viewDidAppear() {
        coreSettingsViewModel.viewDidAppear()
    }
    
    func viewDidDisappear() {
        coreSettingsViewModel.viewDidDisappear()
    }
}
