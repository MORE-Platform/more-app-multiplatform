//
//  NotificationViewModel.swift
//  iosApp
//
//  Created by Isabella Aigner on 12.04.23.
//  Copyright © 2023 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung
//  Licensed under the Apache 2.0 license with Commons Clause
//  (see https://www.apache.org/licenses/LICENSE-2.0 and
//  https://commonsclause.com/).
//

import Combine
import KMPNativeCoroutinesCombine
import shared

class NotificationViewModel: ObservableObject {
    private let filterViewModel: CoreNotificationFilterViewModel
    private let coreModel: CoreNotificationViewModel

    @Published var notificationList: [NotificationModel] = []

    @Published var filterText: String = ""

    private var cancellables = Set<AnyCancellable>()

    init(filterViewModel: CoreNotificationFilterViewModel) {
        self.filterViewModel = filterViewModel
        coreModel = CoreNotificationViewModel(coreFilterModel: filterViewModel, notificationManager: AppDelegate.shared.notificationManager)

        createPublisher(for: coreModel.notificationList)
        .removeDuplicates()
        .receive(on: DispatchQueue.main)
        .sink(receiveCompletion: { _ in }) { [weak self] notifications in
            self?.notificationList = notifications
        }
        .store(in: &cancellables)

        createPublisher(for: filterViewModel.activeTypes)
            .map { (types: Set<String>) -> String in
                guard !types.isEmpty else {
                    return ""
                }

                let localized = types
                    .sorted()
                    .map { String(localized: String.LocalizationValue($0)) }

                return ListFormatter.localizedString(byJoining: localized)
            }
        .removeDuplicates()
        .receive(on: DispatchQueue.main)
        .sink(receiveCompletion: { _ in }) { [weak self] text in
            self?.filterText = text
        }
        .store(in: &cancellables)
    }

    func handleNotificationAction(notification: NotificationModel, navigationModalState: NavigationModalState) {
        coreModel.handleNotificationAction(notification: notification) { actionHandler, data in
            if let data {
                switch actionHandler {
                case NotificationActionHandler.deeplink:
                    AppDelegate.navigationScreenHandler.openRoute(to: data)
                default:
                    return
                }
            }
        }
    }
}
