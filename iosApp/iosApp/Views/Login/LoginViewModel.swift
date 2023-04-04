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
    
    
    private let coreModel: CoreLoginViewModel
    
    var delegate: LoginViewModelListener? = nil

    @Published var isLoading = false
    @Published var endpoint: String = ""
    @Published var token: String = ""
    @Published var error: String = ""
    
    
    init(registrationService: RegistrationService) {
        
        coreModel = CoreLoginViewModel(registrationService: registrationService)
        endpoint = registrationService.getEndpointRepository().endpoint()
        
        coreModel.onLoadingChange { loading in
            if let loading = loading as? Bool {
                self.isLoading = loading
            }
        }
    }
    
    func validate() {
        self.error = ""
        coreModel.sendRegistrationToken(token: token, endpoint: endpoint) { study in
            self.delegate?.tokenValid(study: study)
            DispatchQueue.main.async {
                self.token = ""
            }
        } onError: { error in
            DispatchQueue.main.async {
                self.error = error?.message ?? ""
            }
        }
    }
    
    func checkTokenCount() -> Bool {
        return self.token.count == 0
    }
}

