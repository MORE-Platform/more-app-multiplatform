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

import SwiftUI
import shared

struct NotificationView: View {
    @StateObject var notificationViewModel: NotificationViewModel
    @StateObject var filterVM: NotificationFilterViewModel
    private let navigationStrings = "Navigation"
    private let stringTable = "NotificationView"
    
    @EnvironmentObject private var navigationModalState: NavigationModalState
    
    var body: some View {
        VStack {
            MoreFilter(filterText: $notificationViewModel.filterText, destination: .notificationFilter)
            .padding(.bottom)
            
            if notificationViewModel.notificationList.isEmpty {
                EmptyListView(text: "There are currently no notficiations to show".localize(withComment: "Empty notification list", useTable: stringTable))
            } else {
                ScrollView {
                    ForEach(notificationViewModel.notificationList.sorted{$0.timestamp > $1.timestamp}, id: \.self) { notification in
                        VStack {
                            NotificationItem(notificationModel: notification)
                        }
                        
                        .onTapGesture {
                            if !notification.read {
                                if let deepLinkString = notification.deepLink, let deepLink = URL(string: deepLinkString) {
                                    navigationModalState.openWithDeepLink(url: deepLink, notificationId: notification.notificationId)
                                } else {
                                    notificationViewModel.setNotificationToRead(notification: notification)
                                }
                            }
                            
                        }
                    }
                }
            }
            Spacer()
        }
        .frame(maxWidth: .infinity)
        .onAppear {
            notificationViewModel.getFilterText(stringTable: stringTable)
            notificationViewModel.viewDidAppear()
        }
        .onDisappear {
            notificationViewModel.viewDidDisappear()
        }
        .customNavigationTitle(with: NavigationScreen.notifications.localize(useTable: navigationStrings, withComment: "Navigation title"))

    }
}

