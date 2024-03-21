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
    @EnvironmentObject private var navigationModalState: NavigationModalState
    private let strings = "Navigation"
    var body: some View {
        TabView(selection: $navigationModalState.tagState) {
            Group {
                NavigationWithDestinations {
                    DashboardView(viewModel: contentViewModel.dashboardViewModel)
                }
                .tabItem {
                    Label(NavigationScreen.dashboard.localize(useTable: strings, withComment: "Dashboard Tab"), systemImage: "house")
                }.tag(0)

                NavigationWithDestinations {
                    NotificationView(notificationViewModel: contentViewModel.notificationViewModel, filterVM: contentViewModel.notificationFilterViewModel)
                }
                .tabItem {
                    Label(NavigationScreen.notifications.localize(useTable: strings, withComment: "Notifications Tab"), systemImage: "bell")
                }.tag(1)

                NavigationWithDestinations {
                    InfoView(viewModel: contentViewModel.infoViewModel)
                }
                .tabItem {
                    Label(NavigationScreen.info.localize(useTable: strings, withComment: "Info Tab"), systemImage: "info.circle")
                }.tag(2)
            }
        }
        .accent(color: .more.primaryDark)
        .onAppear {
            UITabBar.appearance().barTintColor = UIColor(Color.more.primaryLight)
            UITabBar.appearance().unselectedItemTintColor = UIColor(Color.more.primary)
            UINavigationBar.appearance().titleTextAttributes = [.foregroundColor: UIColor(Color.more.secondary)]
        }
        .fullScreenCover(isPresented: navigationModalState.screenBinding(for: .questionObservation)) {
            if let navigationState = navigationModalState.navigationState(for: .questionObservation) {
                Navigation {
                    SimpleQuetionObservationView(viewModel: contentViewModel.getSimpleQuestionObservationVM(navigationState: navigationState))
                        .navigationBarTitleDisplayMode(.inline)
                }
                .onDisappear {
                    navigationModalState.removeNavigationAction()
                }
            }
        }
        .fullScreenCover(isPresented: navigationModalState.screenBinding(for: .questionObservationThanks)) {
            Navigation {
                SimpleQuestionThankYouView()
                    .navigationBarTitleDisplayMode(.inline)
            }
            .onDisappear {
                navigationModalState.removeNavigationAction()
            }
        }
        .fullScreenCover(isPresented: navigationModalState.screenBinding(for: .limeSurvey)) {
            Navigation {
                LimeSurveyView(viewModel: contentViewModel.getLimeSurveyVM(navigationModalState: navigationModalState))
                    .navigationBarTitleDisplayMode(.inline)
            }
            .onDisappear {
                navigationModalState.removeNavigationAction()
            }
        }
        .fullScreenCover(isPresented: navigationModalState.screenBinding(for: .withdrawStudy)) {
            LeaveStudyView(viewModel: contentViewModel.settingsViewModel)
        }
        
    }
}

struct MainTabView_Previews: PreviewProvider {
    static var previews: some View {
        MainTabView()
    }
}
