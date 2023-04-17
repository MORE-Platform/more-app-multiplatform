//
//  NotificationView.swift
//  iosApp
//
//  Created by Jan Cortiel on 14.03.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import SwiftUI

struct NotificationView: View {
    @StateObject var notificationViewModel: NotificationViewModel = NotificationViewModel()
    private let navigationStrings = "Navigation"
    private let stringTable = "NotificationView"
    
    var body: some View {
        Navigation {
            MoreMainBackgroundView {
                VStack {
                    MoreFilter(text: .constant(String
                        .localizedString(forKey: "no_filter_activated", inTable: stringTable, withComment: "string if no filter is selected")))
                    .padding(.bottom)
                    
                    
                    ForEach(Array(notificationViewModel.notificationList.enumerated()), id: \.element) { i, notification in
                        
                        VStack {
                            NotificationItem(
                                title: .constant(notification.title ?? "Notification"),
                                message: .constant(notification.notificationBody ?? ""),
                                read: .constant(notification.read),
                                isImportant: .constant((notification.priority == 2))
                            )
                        }
                        .contentShape(Rectangle())
                        .onTapGesture {
                            notificationViewModel.setNotificationToRead(notification: notification)
                        }
                    }                    
                }
            } topBarContent: {
                EmptyView()
            }
            .customNavigationTitle(with: NavigationScreens.notifications.localize(useTable: navigationStrings, withComment: "Navigation title"))
            .navigationBarTitleDisplayMode(.inline)
        }

    }
}

struct NotificationView_Previews: PreviewProvider {
    static var previews: some View {
        NotificationView(notificationViewModel: NotificationViewModel())
    }
}
