//
//  MainTabView.swift
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

struct MainTabView: View {
    @EnvironmentObject var contentViewModel: ContentViewModel
    private let strings = "Navigation"
    var body: some View {
        TabView {
            Group {
                DashboardView(viewModel: contentViewModel.dashboardViewModel)
                    .tabItem {
                        Label(NavigationScreens.dashboard.localize(useTable: strings, withComment: "Dashboard Tab"), systemImage: "house")
                    }
                    .environmentObject(contentViewModel)
                NotificationView(notificationViewModel: contentViewModel.notificationViewModel, filterVM: contentViewModel.notificationFilterViewModel)
                    .tabItem {
                        Label(NavigationScreens.notifications.localize(useTable: strings, withComment: "Notifications Tab"), systemImage: "bell")
                    }
                InfoView(viewModel: contentViewModel.infoViewModel)
                    .tabItem {
                        Label(NavigationScreens.info.localize(useTable: strings, withComment: "Info Tab"), systemImage: "info.circle")
                    }
                    .environmentObject(contentViewModel)
            }
        }
        .accent(color: .more.primaryDark)
        .onAppear {
            UITabBar.appearance().barTintColor = UIColor(Color.more.primaryLight)
            UITabBar.appearance().unselectedItemTintColor = UIColor(Color.more.primary)
            UINavigationBar.appearance().titleTextAttributes = [.foregroundColor: UIColor(Color.more.secondary)]
        }
    }
}

struct MainTabView_Previews: PreviewProvider {
    static var previews: some View {
        MainTabView()
    }
}
