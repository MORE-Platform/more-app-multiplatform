//
//  LoginViewModel.swift
//  iosApp
//
//  Created by Jan Cortiel on 06.02.23.
//  Copyright © 2023 orgName. All rights reserved.
//


import shared

class LoginViewModel: ObservableObject {
    private let userDefaultRepository = UserDefaultsRepository()
    private let endpointRepository: EndpointRepository
    private let credentialReposiotry: CredentialRepository
    private let coreModel: CoreLoginViewModel

    @Published private(set) var isLoading = false
    
    
    init() {
        endpointRepository = EndpointRepository(sharedStorageRepository: userDefaultRepository)
        credentialReposiotry = CredentialRepository(sharedStorageRepository: userDefaultRepository)
        coreModel = CoreLoginViewModel(endpointRepository: endpointRepository, credentialRepository: credentialReposiotry)
        
        coreModel.onStudyChange { study in
            if study != nil {
                print("Study title: \(String(describing: study?.studyTitle))")
            }
        }
        coreModel.onLoadingChange { loading in
            self.isLoading = loading as! Bool
        }
    }
    
    func validate(token: String, endpoint: String? = nil) {
        coreModel.sendRegistrationToken(token: token, endpoint: endpoint)
    }
}
