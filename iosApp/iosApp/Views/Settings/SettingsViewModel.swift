//
//  SettingsViewModel.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 08.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import Foundation
import shared

class SettingsViewModel: ObservableObject {
    private var coreSettingsViewModel: CoreSettingsViewModel? = nil
    private var storageRepository: UserDefaultsRepository = UserDefaultsRepository()
    @Published var study: StudySchema? = StudySchema()
    var delegate: ConsentViewModelListener? = nil
    @Published var dataDeleted = false
    @Published var showSettings = false
    
    init() {
        coreSettingsViewModel = CoreSettingsViewModel(credentialRepository: CredentialRepository(sharedStorageRepository: storageRepository), endpointRepository: EndpointRepository(sharedStorageRepository: storageRepository))
        if let coreSettingsViewModel {
            coreSettingsViewModel.onLoadStudy { study in
                self.study = study
            }
        }
    }
    
    func leaveStudy() {
        if let coreSettingsViewModel {
            coreSettingsViewModel.exitStudy()
            self.delegate?.credentialsDeleted()
        }
    }
    
    func reloadStudyConfig() {
        if let coreSettingsViewModel {
            coreSettingsViewModel.reloadStudyConfig()
        }
    }
}
