//
//  ConsentViewModel.swift
//  iosApp
//
//  Created by Jan Cortiel on 08.02.23.
//  Copyright © 2023 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung
//  Licensed under the Apache 2.0 license with Commons Clause
//  (see https://www.apache.org/licenses/LICENSE-2.0 and
//  https://commonsclause.com/).
//

import AVFoundation
import Combine
import KMPNativeCoroutinesCombine
import UIKit
import shared

class ConsentViewModel: ObservableObject {
    private let coreModel: CoreConsentViewModel
    private let registration: RegistrationService
    var consentInfo: String?
    private let stringTable = "SettingsView"

    @Published var permissionModel: PermissionModel? = nil
    @Published var showErrorAlert: Bool = false
    @Published var requestedPermissions = false

    private var cancellables = Set<AnyCancellable>()

    lazy var permissionManager = PermissionManager()
    var permissionGranted = false

    init(registrationService: RegistrationService) {
        registration = registrationService
        coreModel = CoreConsentViewModel(registrationService: registrationService, studyConsentTitle: String(localized: "study_consent"))

        createPublisher(for: coreModel.permissions)
            .receive(on: DispatchQueue.main)
            .sink { _ in
            } receiveValue: { [weak self] model in
                self?.permissionModel = model
            }
            .store(in: &cancellables)
    }

    func onAppear() {
        permissionManager.observer = self
        coreModel.viewDidAppear()
    }

    func onDisappear() {
        permissionManager.observer = nil
        coreModel.viewDidDisappear()
    }

    func resetPermissionRequest() {
        requestedPermissions = false
        permissionManager.resetRequest()
    }

    func requestPermissions() {
        requestedPermissions = true
        permissionManager.requestPermission(permissionRequest: true)
    }

    private func acceptConsent() {
        if let uniqueId = UIDevice.current.identifierForVendor?.uuidString {
            registration.acceptConsent(uniqueDeviceId: uniqueId)
        }
    }

    func decline() {
        registration.declineConsent()
    }
}

extension ConsentViewModel: PermissionManagerObserver {
    func accepted() {
        Task { @MainActor in
            // Delegates to HealthConnectObservation's own collector-aware permission check (the
            // same logic already used at schedule-start time) instead of a hardcoded HealthKit
            // request here - it scopes to whichever subtypes the study actually needs, requests
            // only what's missing, and shows its own alert on decline.
            try? await AppDelegate.shared.observationFactory
                .observation(type: ObservationTypeEnum.healthConnect.value)?
                .updateObservationPermissions()
            if permissionManager.anyNeededPermissionDeclined() {
                AlertController.shared.openAlertDialog(
                    model:
                        AlertDialogModel.companion.fromStrings(
                            title: "Required Permissions Were Not Granted",
                            message: "This study requires one or more sensor permissions to function correctly. You may choose to decline these permissions; however, doing so may result in the application and study not functioning fully or as expected. Would you like to navigate to settings to allow the app access to these necessary permissions?",
                            confirmLabel: "Proceed to Settings",
                            cancelLabel: "Proceed Without Granting Permissions",
                            onConfirm: {
                                if let url = URL(string: UIApplication.openSettingsURLString), UIApplication.shared.canOpenURL(url) {
                                    UIApplication.shared.open(url, options: [:], completionHandler: nil)
                                }
                                self.resetPermissionRequest()
                            },
                            onDecline: {
                                self.acceptConsent()
                                self.requestedPermissions = false
                            }))
            } else {
                self.acceptConsent()
                self.requestedPermissions = false
            }
        }
    }
}
