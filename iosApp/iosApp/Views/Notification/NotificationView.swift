//
//  NotificationView.swift
//  iosApp
//
//  Created by Jan Cortiel on 14.03.23.
//  Copyright © 2023 orgName. All rights reserved.
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
                    
                    // add for each loop for notificationlist and send each in an NotificationItem
                    
                    NotificationItem(
                        title: .constant("Test Notification"),
                        message: .constant("Some Message"),
                        read: .constant(false),
                        isImportant: .constant(true)
                    )
                    NotificationItem(
                        title: .constant("Test Notification"),
                        message: .constant("Some Message"),
                        read: .constant(true),
                        isImportant: .constant(true)
                    )
                    NotificationItem(
                        title: .constant("Test Notification"),
                        message: .constant("Some Message"),
                        read: .constant(false),
                        isImportant: .constant(false)
                    )
                    NotificationItem(
                        title: .constant("Test Notification"),
                        message: .constant("Some Message"),
                        read: .constant(true),
                        isImportant: .constant(false)
                    )
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
