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


import shared


class NotificationViewModel: ObservableObject {
    let recorder = IOSDataRecorder()
    private let filterViewModel: CoreNotificationFilterViewModel
    private let coreModel: CoreNotificationViewModel

    @Published var notificationList: [NotificationModel] = []
    @Published var filterText: String = "FilterText"

    init(filterViewModel: CoreNotificationFilterViewModel) {
        self.filterViewModel = filterViewModel
        self.coreModel = CoreNotificationViewModel(coreFilterModel: filterViewModel, notificationManager: AppDelegate.shared.notificationManager, protocolReplacement: nil, hostReplacement: nil)
        coreModel.onNotificationLoad { [weak self] notifications in
            DispatchQueue.main.async {
                self?.notificationList = []
                self?.notificationList = notifications
            }
        }
    }

    func handleNotificationAction(notification: NotificationModel, navigationModalState: NavigationModalState) {
        coreModel.handleNotificationAction(notification: notification) { deepLink in
            if let uri = URL(string: deepLink) {
                navigationModalState.openWithDeepLink(url: uri, notificationId: notification.notificationId)
            }
        }
    }

    func viewDidAppear() {
        coreModel.viewDidAppear()
    }

    func viewDidDisappear() {
        coreModel.viewDidDisappear()
    }

    func getFilterText(stringTable: String) {
        self.filterText = filterViewModel.getActiveTypes().map {
            $0.localize(withComment: $0, useTable: stringTable)
        }
        .joined(separator: ", ")
    }
}
