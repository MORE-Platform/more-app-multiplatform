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
import KMPNativeCoroutinesCombine
import Combine

class LoginViewModel: ObservableObject {
    private let registrationService: RegistrationService
    @Published var endpoint: String = ""
    @Published var defaultEndpoint: String = ""
    @Published var token: String = ""
    
    @Published var showQRCodeView: Bool = false
    
    private var cancellables: Set<AnyCancellable> = []
    
    
    init(registration: RegistrationService) {
        self.registrationService = registration
        defaultEndpoint = registration.getEndpointRepository().endpoint()
        
        Publishers.CombineLatest($endpoint, $token)
            .map { !$0.isEmpty || !$1.isEmpty }
            .removeDuplicates()
            .sink { [weak self] hasInput in
                self?.registrationService.clearError()
            }
            .store(in: &cancellables)
    }
    
    func extractValuesFromQRCode(qrCodeUrl: String) {
        endpoint = qrCodeUrl.components(separatedBy: "signup?").first ?? ""
         
         if let tokenPart = qrCodeUrl.components(separatedBy: "token=").last,
               tokenPart != qrCodeUrl {
                token = tokenPart.components(separatedBy: "&").first ?? ""
         }
     }
    
    func validate() {
        let loginModel = LoginModel(token: token, endpoint: currentStudyEndpoint())
        if loginModel.valid() {
            registrationService.sendRegistrationToken(loginModel: loginModel)
        } else {
            AlertController.shared.openAlertDialog(model: AlertDialogModel(title: "Token or Endpoint invalid", message: "login_model_invalid_body", positiveTitle: "Ok", negativeTitle: nil, onPositive: {
                AlertController.shared.closeAlertDialog()
            }, onNegative: nil))
        }
    }
    
    func checkTokenCount() -> Bool {
        return self.token.count == 0
    }
    
    func currentStudyEndpoint() -> String {
        endpoint.isEmpty ? defaultEndpoint : endpoint
    }
}

