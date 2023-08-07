//
//  NotificationView.swift
//  iosApp
//
//  Created by Jan Cortiel on 14.03.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import SwiftUI
import shared

struct NotificationView: View {
    @StateObject var notificationViewModel: NotificationViewModel
    @StateObject var filterVM: NotificationFilterViewModel
    private let navigationStrings = "Navigation"
    private let stringTable = "NotificationView"
    
    var body: some View {
        Navigation {
            MoreMainBackgroundView {
                VStack {
                    MoreFilter(filterText: $notificationViewModel.filterText) {
                        NotificationFilterView(viewModel: filterVM)
                    }
                    .onAppear{
                        notificationViewModel.getFilterText(stringTable: stringTable)
                    }
                    .padding(.bottom)
                    
                    if notificationViewModel.notificationList.isEmpty {
                        EmptyListView(text: "There are currently no notficiations to show".localize(withComment: "Empty notification list", useTable: stringTable))
                    } else {
                        ScrollView {
                            ForEach(notificationViewModel.notificationList.sorted{$0.timestamp > $1.timestamp}, id: \.self) { notification in
                                VStack {
                                    NotificationItem(
                                        title: notification.title,
                                        message: notification.notificationBody,
                                        read: notification.read,
                                        isImportant: (notification.priority == 2),
                                        timestamp: notification.timestamp
                                    )
                                }
                                .contentShape(Rectangle())
                                .onTapGesture {
                                    notificationViewModel.setNotificationToRead(notification: notification)
                                }
                            }
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
            .customNavigationTitle(with: NavigationScreens.notifications.localize(useTable: navigationStrings, withComment: "Navigation title"))
        }

    }
}

