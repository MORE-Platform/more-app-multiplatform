//
//  NotificationViewModel.swift
//  iosApp
//
//  Created by Isabella Aigner on 12.04.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//


import shared


class NotificationViewModel: ObservableObject {
    let filterModel: NotificationFilterViewModel = NotificationFilterViewModel()
    let recorder = IOSDataRecorder()
    private let coreModel: CoreNotificationViewModel
    
    @Published var notificationList: [NotificationModel] = []
    @Published var notificationCount: Int64 = 0
    @Published var filterText: String = "FilterText"

    init() {
        self.coreModel = CoreNotificationViewModel(coreFilterModel: filterModel.coreModel)
        self.notificationList = []
        
        coreModel.onNotificationLoad { notifications in
            self.notificationList = notifications
        }
        
        coreModel.onCountLoad { count in
            self.notificationCount = count?.int64Value ?? 0
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

    func updateFilterText() {
        self.filterText = filterModel.getFilterText()
    }
}
