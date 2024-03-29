//
//  LoginViewModel.swift
//  iosApp
//
//  Created by Jan Cortiel on 06.02.23.
//  Copyright © 2023 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung 
//  Licensed under the Apache 2.0 license with Commons Clause 
//  (see https://www.apache.org/licenses/LICENSE-2.0 and
//  https://commonsclause.com/).
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
    @Published var defaultEndpoint: String = ""
    @Published var token: String = ""
    @Published var error: String = ""
    
    
    init(registrationService: RegistrationService) {
        print("LoginViewModel allocated!")
        coreModel = CoreLoginViewModel(registrationService: registrationService)
        defaultEndpoint = registrationService.getEndpointRepository().endpoint()
        
        coreModel.onLoadingChange { loading in
            if let loading = loading as? Bool {
                self.isLoading = loading
            }
        }
    }
    
    func validate() {
        self.error = ""
        coreModel.sendRegistrationToken(token: token, endpoint: endpoint.isEmpty ? nil : endpoint) { study in
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
    
    func currentStudyEndpoint() -> String {
        endpoint.isEmpty ? defaultEndpoint : endpoint
    }
    
    deinit {
        print("LoginViewModel deallocated")
    }
}

