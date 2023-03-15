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
    var body: some View {
        TabView {
            DashboardView(dashboardViewModel: contentViewModel.dashboardViewModel)
                .tabItem {
                    Label("Dashboard", systemImage: "house")
                }
            NotificationView()
                .tabItem {
                    Label("Notifications", systemImage: "bell")
                }
            InfoView()
                .tabItem {
                    Label("Info", systemImage: "info.circle")
                }
        }
    }
}

struct MainTabView_Previews: PreviewProvider {
    static var previews: some View {
        MainTabView()
    }
}
