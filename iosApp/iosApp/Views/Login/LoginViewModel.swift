//
//  LoginViewModel.swift
//  iosApp
//
//  Created by Jan Cortiel on 06.02.23.
//  Copyright © 2023 orgName. All rights reserved.
//


import shared

protocol LoginViewModelListener {
    func tokenValid(study: Study)
}

class LoginViewModel: ObservableObject {
    private let userDefaultRepository = UserDefaultsRepository()
    private let endpointRepository: EndpointRepository
    private let coreModel: CoreLoginViewModel
    
    var delegate: LoginViewModelListener? = nil

    @Published private(set) var isLoading = false
    @Published var endpoint: String = ""
    @Published var token: String = ""
    
    
    init(registrationService: RegistrationService) {
        endpointRepository = EndpointRepository(sharedStorageRepository: userDefaultRepository)
        
        self.endpoint = endpointRepository.endpoint()
        
        coreModel = CoreLoginViewModel(registrationService: registrationService)
        
        coreModel.onLoadingChange { loading in
            if let loading = loading as? Bool {
                self.isLoading = loading
            }
        }
    }
    
    func validate() {
        coreModel.sendRegistrationToken(token: token, endpoint: endpoint) { study in
            self.delegate?.tokenValid(study: study)
        } onError: { error in
            print(error?.message)
        }
    }
}
