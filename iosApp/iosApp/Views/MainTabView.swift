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
//  Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
//

import SwiftUI

struct MainTabView: View {
    @EnvironmentObject var contentViewModel: ContentViewModel
    @EnvironmentObject private var navigationModalState: NavigationModalState
    var body: some View {
        TabView(selection: $navigationModalState.tagState) {
            Group {
                NavigationWithDestinations {
                    DashboardView(viewModel: contentViewModel.manualSchedule)
                        .padding(.horizontal, navigationModalState.horizontalContentPadding)
                }
                .tabItem {
                    Label(NavigationScreen.dashboard.localize(), systemImage: "house")
                }
                .tag(0)

                NavigationWithDestinations {
                    NotificationView(coreFilterVM: contentViewModel.coreNotificationFilterViewModel)
                        .padding(.horizontal, navigationModalState.horizontalContentPadding)
                }
                .tabItem {
                    Label(NavigationScreen.notifications.localize(), systemImage: "bell")
                }
                .tag(1)
                NavigationWithDestinations {
                    InfoView(viewModel: contentViewModel.infoViewModel)
                        .padding(.horizontal, navigationModalState.horizontalContentPadding)
                }
                .tabItem {
                    Label(NavigationScreen.info.localize(), systemImage: "info.circle")
                }
                .tag(2)
            }
        }
        .tint(.more.primaryDark)
        .onAppear {
            UITabBar.appearance().barTintColor = UIColor(Color.more.primaryLight)
            UITabBar.appearance().unselectedItemTintColor = UIColor(Color.more.primary)
            UINavigationBar.appearance().titleTextAttributes = [.foregroundColor: UIColor(Color.more.secondary)]
        }
        .fullScreenCover(isPresented: navigationModalState.screenBinding(for: .questionObservation)) {
            if let navigationState = navigationModalState.navigationState(for: .questionObservation) {
                Navigation {
                    QuestionObservationView(navigationState: navigationState)
                        .navigationBarTitleDisplayMode(.inline)
                }
                .onDisappear {
                    navigationModalState.removeNavigationAction()
                }
            }
        }
        .fullScreenCover(isPresented: navigationModalState.screenBinding(for: .questionObservationThanks)) {
            Navigation {
                QuestionThankYouView()
                    .navigationBarTitleDisplayMode(.inline)
            }
            .onDisappear {
                navigationModalState.removeNavigationAction()
            }
        }
        .fullScreenCover(isPresented: navigationModalState.screenBinding(for: .limeSurvey)) {
            if let navigationState = navigationModalState.navigationState(for: .limeSurvey) {
                Navigation {
                    LimeSurveyView(navigationState: navigationState)
                        .navigationBarTitleDisplayMode(.inline)
                }
                .onDisappear {
                    navigationModalState.removeNavigationAction()
                }
            }
        }
        .fullScreenCover(isPresented: navigationModalState.screenBinding(for: .withdrawStudy)) {
            LeaveStudyView()
        }
        .fullScreenCover(isPresented: navigationModalState.screenBinding(for: .garminConnect)) {
            Navigation {
                GarminConnectView()
                    .navigationBarTitleDisplayMode(.inline)
            }
            .onDisappear {
                navigationModalState.removeNavigationAction()
            }
        }
    }
}

struct MainTabView_Previews: PreviewProvider {
    static var previews: some View {
        MainTabView()
    }
}

