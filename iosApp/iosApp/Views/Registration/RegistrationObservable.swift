//
//  RegistrationObservable.swift
//  More
//
//  Created by Jan Cortiel on 15.09.25.
//  Copyright © 2025 Redlink GmbH. All rights reserved.
//

import Combine
import Foundation
import KMPNativeCoroutinesCombine
import shared

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

        createPublisher(for: service.validLoginModel)
        .removeDuplicates()
        .receive(on: DispatchQueue.main)
        .sink { _ in
        } receiveValue: { [weak self] model in
            self?.validLoginModel = model
        }
        .store(in: &cancellables)

        createPublisher(for: service.study_)
        .receive(on: DispatchQueue.main)
        .sink { _ in
        } receiveValue: { [weak self] study in
            self?.study = study
        }
        .store(in: &cancellables)

        createPublisher(for: service.error)
        .removeDuplicates()
        .receive(on: DispatchQueue.main)
        .sink { _ in
        } receiveValue: { [weak self] error in
            self?.error = error
            if error != nil && self?.study != nil {
                AlertController.shared.openAlertDialog(model: AlertDialogModel(
                    title: "consent_error_title",
                    message: "consent_error_body",
                    confirmLabel: "Ok",
                    cancelLabel: nil,
                    onConfirm: nil)
                )
            }
        }
        .store(in: &cancellables)

        createPublisher(for: service.isLoading)
        .removeDuplicates()
        .receive(on: DispatchQueue.main)
        .sink { _ in
        } receiveValue: { [weak self] isLoading in
            self?.isLoading = isLoading.boolValue
        }
        .store(in: &cancellables)

        createPublisher(for: service.connected)
        .removeDuplicates()
        .receive(on: DispatchQueue.main)
        .sink { _ in
        } receiveValue: { [weak self] connected in
            self?.connected = connected.boolValue
        }
        .store(in: &cancellables)
    }
}
