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
                    Text("Here should be a filter")
                        .padding(.bottom)
                    
                    ScrollView {
                        ForEach(Array(notificationViewModel.notificationList.enumerated()), id: \.element) { i, notification in
                            
                            VStack {
                                NotificationItem(
                                    title: .constant(notification.title),
                                    message: .constant(notification.notificationBody),
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
                    .onAppear {
                        notificationViewModel.viewDidAppear()
                    }
                    .onDisappear {
                        notificationViewModel.viewDidDisappear()
                    }
                }
            }
            .customNavigationTitle(with: NavigationScreens.notifications.localize(useTable: navigationStrings, withComment: "Navigation title"), displayMode: .inline)
            .navigationBarTitleDisplayMode(.inline)
        }

    }
}

struct NotificationView_Previews: PreviewProvider {
    static var previews: some View {
        NotificationView(notificationViewModel: NotificationViewModel())
    }
}
