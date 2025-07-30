//
//  MoreNavigationView.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 13.03.23.
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

struct NavigationWithStack<Content: View>: View {
    var content: () -> Content

    @EnvironmentObject private var navigationModalState: NavigationModalState
    @EnvironmentObject private var contentViewModel: ContentViewModel
    @State private var navigationPath = NavigationPath()
    @State private var actionsSet = false

    var body: some View {
        NavigationStack(path: $navigationPath) {
            content()
                .background(Color.more.mainBackground)
                .navigationBarTitleDisplayMode(.inline)
                .onChange(of: navigationPath.count) { count in
                    if count < navigationModalState.navigationStack.count {
                        navigationModalState.popNavigationStack()
                    }
                }
                .environmentObject(navigationModalState)
                .environmentObject(contentViewModel)
        }
        .onAppear {
            navigationModalState.pushNavigationAction(actions: NavigationActions(onViewOpen: onViewOpen, onBack: onBack, onReset: onReset))
        }
    }

    func onViewOpen(screen: NavigationScreen) {
        navigationPath.append(screen)
    }

    func onBack() {
        if !navigationPath.isEmpty {
            navigationPath.removeLast()
        }
    }

    func onReset() {
        navigationPath = NavigationPath()
    }
}

struct Navigation<Content: View>: View {
    var content: () -> Content
    @EnvironmentObject private var navigationModalState: NavigationModalState
    @EnvironmentObject private var contentViewModel: ContentViewModel
    var body: some View {
        VStack {
            NavigationWithStack(content: content)
        }
        .background(Color.more.mainBackground)
    }
}

struct NavigationWithDestinations<Content: View>: View {
    @EnvironmentObject private var navigationModalState: NavigationModalState
    @EnvironmentObject private var contentViewModel: ContentViewModel
    var content: () -> Content
    var body: some View {
        Navigation {
            VStack {
                content()
                    .background(Color.more.mainBackground)
                    .navigationDestination(for: NavigationScreen.self) { screen in
                        viewForScreen(screen)
                    }
            }
        }
    }
    
    @ViewBuilder
    private func viewForScreen(_ screen: NavigationScreen) -> some View {
        MoreMainBackgroundView(contentPadding: navigationModalState.horizontalContentPadding) {
            VStack {
                switch screen {
                case .taskDetails:
                    if let navigationState = navigationModalState.navigationState(for: screen) {
                        TaskDetailsView(viewModel: contentViewModel.getTaskDetailsVM(navigationState: navigationState))
                    } else {
                        EmptyView()
                    }
                case .settings:
                    SettingsView(viewModel: SettingsViewModel())
                case .studyDetails:
                    StudyDetailsView(viewModel: StudyDetailsViewModel())
                case .dashboardFilter:
                    DashboardFilterView(viewModel: contentViewModel.dashboardViewModel.scheduleViewModel.filterViewModel)
                case .notificationFilter:
                    NotificationFilterView(viewModel: contentViewModel.notificationFilterViewModel)
                case .pastObservations:
                    CompletedSchedules(scheduleViewModel: contentViewModel.completedViewModel)
                case .runningObservations:
                    RunningSchedules(scheduleViewModel: contentViewModel.runningViewModel)
                case .bluetoothConnections:
                    BluetoothConnectionView(viewModel: contentViewModel.bluetoothViewModel, viewOpen: .constant(false))
                case .observationDetails:
                    if let observationId = navigationModalState.navigationState(for: screen)?.observationId {
                        ObservationDetailsView(viewModel: ObservationDetailsViewModel(observationId: observationId))
                    }
                case .observationErrors:
                    ObservationErrorsView()
                default:
                    EmptyView()
                }
            }
        }
    }
}

struct NavigationTitleViewModifier: ViewModifier {
    var text: String
    var displayMode: NavigationBarItem.TitleDisplayMode = .automatic

    func body(content: Content) -> some View {
        content
            .navigationTitle(text)
            .navigationBarTitleDisplayMode(displayMode)
    }
}

enum Capitalization {
    case uppercase, lowercase, normal
}

extension View {
    @ViewBuilder
    func customNavigationTitle(with text: String, displayMode: NavigationBarItem.TitleDisplayMode = .inline) -> some View {
        self.modifier(NavigationTitleViewModifier(text: text, displayMode: displayMode))
    }

    @ViewBuilder
    func textFieldAutoCapitalizataion(capitalization: Capitalization) -> some View {
        if capitalization == .uppercase {
            self.modifier(TextFieldViewModifier(capitalization: .characters))
        } else if capitalization == .lowercase {
            self.modifier(TextFieldViewModifier(capitalization: .never))
        } else {
            self.modifier(TextFieldViewModifier(capitalization: .sentences))
        }
    }

    func eraseToAnyView() -> AnyView {
        AnyView(self)
    }
}

struct TextFieldViewModifier: ViewModifier {
    var capitalization: TextInputAutocapitalization = .words
    func body(content: Content) -> some View {
        content.textInputAutocapitalization(capitalization)
    }
}
