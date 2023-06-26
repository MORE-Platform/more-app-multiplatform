//
//  NotificationViewModel.swift
//  iosApp
//
//  Created by Isabella Aigner on 12.04.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
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
        self.coreModel = CoreNotificationViewModel(coreFilterModel: filterViewModel)
        coreModel.onNotificationLoad { [weak self] notifications in
            print("Fetched notifications")
            DispatchQueue.main.async {
                self?.notificationList = []
                self?.notificationList = notifications
            }
        }
    }
    
    func setNotificationToRead(notification: NotificationModel) {
        coreModel.setNotificationReadStatus(notification: notification)
    }

    func viewDidAppear() {
        coreModel.viewDidAppear()
    }

    func viewDidDisappear() {
        coreModel.viewDidDisappear()
    }

    func getFilterText(stringTable: String) {
        self.filterText = filterViewModel.getActiveTypes().map{$0.localize(withComment: $0, useTable: stringTable)}.joined(separator: ", ")
    }
}
