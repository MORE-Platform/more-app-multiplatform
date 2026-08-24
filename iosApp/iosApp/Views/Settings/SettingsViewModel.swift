//
//  SettingsViewModel.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 08.03.23.
//  Copyright © 2023 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung
//  Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
//

import AppTrackingTransparency
import Combine
import Foundation
import KMPNativeCoroutinesCombine
import SwiftUI
import shared

class SettingsViewModel: ObservableObject {
    let coreViewModel: CoreSettingsViewModel

    @Published var studyTitle: String?
    @Published var permissionModel: PermissionModel?
    @Published var showSettings = false
    @Published var allowTracking = ATTrackingManager.trackingAuthorizationStatus == .authorized
    @Published var needsTracking = false

    private var cancellables: Set<AnyCancellable> = []
    private let permissionManager = PermissionManager()

    init(viewIdentifier: String = NavigationRoute.settings.viewIdentifier) {
        coreViewModel = CoreSettingsViewModel(mainRepository: AppDelegate.shared.repositories, sharedStorageRepository: AppDelegate.shared.sharedStorageRepository, customViewIdentifier: viewIdentifier)
        coreViewModel.setExitStudyObserver(observer: AppDelegate.shared)
        createPublisher(for: coreViewModel.study)
            .receive(on: DispatchQueue.main)
            .sink(receiveCompletion: { _ in }) { [weak self] studyEntity in
                self?.studyTitle = studyEntity?.studyTitle
            }
            .store(in: &cancellables)

        createPublisher(for: coreViewModel.permissionModel)
            .receive(on: DispatchQueue.main)
            .sink(receiveCompletion: { _ in }) { [weak self] permissions in
                self?.permissionModel = permissions
            }
            .store(in: &cancellables)

        createPublisher(for: coreViewModel.allowTracking)
            .removeDuplicates()
            .receive(on: DispatchQueue.main)
            .sink(receiveCompletion: { _ in }) { [weak self] allowTracking in
                if ATTrackingManager.trackingAuthorizationStatus == .authorized {
                    self?.allowTracking = allowTracking.boolValue
                }
            }
            .store(in: &cancellables)

        createPublisher(for: coreViewModel.needsTracking)
            .removeDuplicates()
            .receive(on: DispatchQueue.main)
            .sink(receiveCompletion: { _ in }) { [weak self] needsTracking in
                self?.needsTracking = needsTracking.boolValue
            }
            .store(in: &cancellables)
    }

    var allowTrackingBinding: Binding<Bool> {
        Binding<Bool>(
            get: { self.allowTracking },
            set: { [weak self] newValue in
                if ATTrackingManager.trackingAuthorizationStatus != .denied {
                    if newValue {
                        Task { @MainActor in
                            self?.permissionManager.requestAppTrackingAuthorization()
                        }
                    }
                    self?.allowTracking = newValue
                    self?.coreViewModel.setTrackingPermission(allow: newValue)
                } else {
                    self?.coreViewModel.showAppTrackingPermissionDialog()
                }
            }
        )
    }

    func leaveStudy() {
        coreViewModel.exitStudy()
    }
}
