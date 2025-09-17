//
//  NotificationView.swift
//  iosApp
//
//  Created by Jan Cortiel on 14.03.23.
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
import SwiftUI

struct NotificationView: View {
    @StateObject private var notificationViewModel: NotificationViewModel

    @EnvironmentObject private var navigationModalState: NavigationModalState
    
    init(coreFilterVM: CoreNotificationFilterViewModel) {
        _notificationViewModel = StateObject(wrappedValue: NotificationViewModel(filterViewModel: coreFilterVM))
    }

    var body: some View {
        VStack {
            MoreFilter(filterText: $notificationViewModel.filterText, destination: .notificationFilter)
                .padding(.bottom)

            if notificationViewModel.notificationList.isEmpty {
                EmptyListView(text: "There are currently no notficiations to show")
            } else {
                ScrollViewReader { _ in
                    ScrollView {
                        LazyVStack(alignment: .leading, spacing: 0) {
                            ForEach(notificationViewModel.notificationList.sorted { $0.timestamp > $1.timestamp }, id: \.self) { notification in
                                VStack {
                                    NotificationItem(notificationModel: notification)
                                    Divider()
                                        .padding(.vertical, 4)
                                }
                                .background(Color.clear)
                                .contentShape(Rectangle())
                                .onTapGesture {
                                    if !notification.read {
                                        notificationViewModel.handleNotificationAction(notification: notification, navigationModalState: navigationModalState)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        .frame(maxWidth: .infinity)
        .customNavigationTitle(with: NavigationScreen.notifications.localize())
    }
}
