//
//  MainTabView.swift
//  iosApp
//
//  Created by Jan Cortiel on 14.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI

struct MainTabView: View {
    @EnvironmentObject var contentViewModel: ContentViewModel
    private let strings = "Navigation"
    var body: some View {
        TabView {
            Group {
                DashboardView(dashboardViewModel: contentViewModel.dashboardViewModel)
                    .tabItem {
                        Label(NavigationScreens.dashboard.localize(useTable: strings, withComment: "Dashboard Tab"), systemImage: "house")
                    }
                NotificationView()
                    .tabItem {
                        Label(NavigationScreens.notifications.localize(useTable: strings, withComment: "Notifications Tab"), systemImage: "bell")
                    }
                InfoView()
                    .tabItem {
                        Label(NavigationScreens.info.localize(useTable: strings, withComment: "Info Tab"), systemImage: "info.circle")
                    }
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
