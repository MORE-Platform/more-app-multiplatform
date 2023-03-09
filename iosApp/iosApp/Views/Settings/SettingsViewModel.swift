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
    
    init() {
        coreSettingsViewModel = CoreSettingsViewModel(credentialRepository: CredentialRepository(sharedStorageRepository: storageRepository), endpointRepository: EndpointRepository(sharedStorageRepository: storageRepository))
    }
    
    func createCoreViewModel() {
        
    }
    
    func reloadStudyConfig() {
        if let coreSettingsViewModel {
            coreSettingsViewModel.reloadStudyConfig()
        }
    }
}
