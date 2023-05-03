//
//  NotificationViewModel.swift
//  iosApp
//
//  Created by Isabella Aigner on 12.04.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//


import shared


class NotificationViewModel: ObservableObject {
    // private let coreModel
    
    let recorder = IOSDataRecorder()
    private let coreModel: CoreNotificationViewModel = CoreNotificationViewModel(coreFilterModel: CoreNotificationFilterViewModel())
    
    @Published var notificationList: [NotificationModel] = []
    @Published var notificationCount: Int64 = 0
    
    init() {
        self.notificationList = []
        
        coreModel.onNotificationLoad { notifications in
            if !notifications.isEmpty {
                self.notificationList = notifications
            }
        }
        
        coreModel.onCountLoad { count in
            self.notificationCount = count?.int64Value ?? 0
        }
    }
    
    func setNotificationToRead(notification: NotificationModel) {
        coreModel.setNotificationReadStatus(notification: notification)
    }
}
