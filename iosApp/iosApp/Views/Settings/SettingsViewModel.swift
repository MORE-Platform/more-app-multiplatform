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
    private var coreSettingsViewModel: CoreSettingsViewModel
    private var storageRepository: UserDefaultsRepository = UserDefaultsRepository()
    @Published var study: StudySchema?
    @Published private(set) var permissionModel: PermissionModel?
    var delegate: ConsentViewModelListener? = nil
    @Published var dataDeleted = false
    @Published var showSettings = false
    
    init() {
        coreSettingsViewModel = CoreSettingsViewModel(shared: AppDelegate.shared)
        coreSettingsViewModel.onLoadStudy { study in
            self.study = study
        }
        coreSettingsViewModel.onPermissionChange { permissions in
            self.permissionModel = permissions
        }
    }
    
    func leaveStudy() {
        AppDelegate.recorder.stopAll()
        coreSettingsViewModel.exitStudy()
        self.delegate?.credentialsDeleted()
        FCMService().deleteNotificationToken()
    }
    
    func viewDidAppear() {
        coreSettingsViewModel.viewDidAppear()
    }
    
    func viewDidDisappear() {
        coreSettingsViewModel.viewDidDisappear()
    }
}
