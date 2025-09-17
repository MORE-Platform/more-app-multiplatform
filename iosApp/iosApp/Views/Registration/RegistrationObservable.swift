//
//  RegistrationObservable.swift
//  More
//
//  Created by Jan Cortiel on 15.09.25.
//  Copyright © 2025 Redlink GmbH. All rights reserved.
//

import Foundation
import shared
import KMPNativeCoroutinesCombine
import Combine

class RegistrationObservable: ObservableObject {
    let service: RegistrationService
    
    @Published var validLoginModel: LoginModel?
    @Published var study: Study?
    @Published var error: NetworkServiceError?
    @Published var isLoading: Bool = false
    @Published var connected: Bool = false
    
    private var cancellables: Set<AnyCancellable> = []
    
    
    init(service: RegistrationService) {
        self.service = service
    }
    
    func onAppear() {
        createPublisher(for: service.validLoginModel)
            .sink { completion in
                print("Completion")
            } receiveValue: { [weak self] model in
                self?.validLoginModel = model
            }.store(in: &cancellables)
        
        createPublisher(for: service.study)
            .sink { completion in
                print("Study Completion")
            } receiveValue: { [weak self] study in
                self?.study = study
            }.store(in: &cancellables)
        
        createPublisher(for: service.error)
            .sink { completion in
                print("Error completion")
            } receiveValue: { [weak self] error in
                self?.error = error
                if error != nil && self?.study != nil {
                    AlertController.shared.openAlertDialog(model: AlertDialogModel(title: "consent_error_title", message: "consent_error_body", positiveTitle: "Ok", negativeTitle: nil, onPositive: nil))
                }
            }.store(in: &cancellables)
        
        createPublisher(for: service.isLoading)
            .sink { completion in
                print("Loading completion")
            } receiveValue: { [weak self] isLoading in
                self?.isLoading = isLoading.boolValue
            }.store(in: &cancellables)
        
        createPublisher(for: service.connected)
            .sink { completed in
                print("Connected sink completed: \(completed)")
            } receiveValue: { [weak self] connected in
                self?.connected = connected.boolValue
            }.store(in: &cancellables)
    }
    
    func onDisappear() {
        self.cancellables.forEach { $0.cancel() }
        self.cancellables.removeAll()
    }
}
