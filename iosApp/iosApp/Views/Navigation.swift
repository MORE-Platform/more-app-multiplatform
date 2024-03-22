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

@available(iOS 16.0, *)
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
            if #available(iOS 16.0, *) {
                NavigationWithStack(content: content)
            } else {
                NavigationView {
                    content()
                        .background(Color.more.mainBackground)
                        .navigationBarTitleDisplayMode(.inline)
                        .environmentObject(navigationModalState)
                        .environmentObject(contentViewModel)
                }
                .background(Color.more.mainBackground)
            }
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
                if #available(iOS 16.0, *) {
                    content()
                        .background(Color.more.mainBackground)
                        .navigationDestination(for: NavigationScreen.self) { screen in
                            viewForScreen(screen)
                        }
                } else {
                    ForEach(NavigationScreen.allCases) { screen in
                        if screen == navigationModalState.currentScreen() {
                            NavigationLink(destination: viewForOldScreen(screen), isActive: navigationModalState.screenBinding(for: screen)) {
                                EmptyView()
                            }
                            .opacity(0)
                        }
                    }

                    content()
                        .background(Color.more.mainBackground)
                }
            }
        }
    }
    
    
    @ViewBuilder
    private func viewForOldScreen(_ screen: NavigationScreen) -> some View {
        VStack {
            ForEach(NavigationScreen.allCases) { screen in
                if screen == navigationModalState.currentScreen() {
                    NavigationLink(destination: viewForScreen(screen), isActive: navigationModalState.screenBinding(for: screen)) {
                        EmptyView()
                    }
                    .opacity(0)
                }
            }
            viewForScreen(screen)
        }
    }
    
    @ViewBuilder
    private func viewForScreen(_ screen: NavigationScreen) -> some View {
        MoreMainBackgroundView {
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
                default:
                    EmptyView()
                }
            }
        }
    }
}

@available(iOS 16, *)
struct NavigationTitleViewModifier: ViewModifier {
    var text: String
    var displayMode: NavigationBarItem.TitleDisplayMode = .automatic

    func body(content: Content) -> some View {
        content
            .navigationTitle(text)
            .navigationBarTitleDisplayMode(displayMode)
    }
}

struct NavigationBarTitleViewModifier: ViewModifier {
    var text: String
    var displayMode: NavigationBarItem.TitleDisplayMode = .automatic

    func body(content: Content) -> some View {
        content
            .navigationBarTitle(text, displayMode: displayMode)
    }
}

enum Capitalization {
    case uppercase, lowercase, normal
}

extension View {
    @ViewBuilder
    func customNavigationTitle(with text: String, displayMode: NavigationBarItem.TitleDisplayMode = .inline) -> some View {
        if #available(iOS 16, *) {
            self.modifier(NavigationTitleViewModifier(text: text, displayMode: displayMode))
        } else {
            modifier(NavigationBarTitleViewModifier(text: text, displayMode: displayMode))
        }
    }

    @ViewBuilder
    func textFieldAutoCapitalizataion(capitalization: Capitalization) -> some View {
        if #available(iOS 15, *) {
            if capitalization == .uppercase {
                self.modifier(TextFieldViewModifier(capitalization: .characters))
            } else if capitalization == .lowercase {
                self.modifier(TextFieldViewModifier(capitalization: .never))
            } else {
                self.modifier(TextFieldViewModifier(capitalization: .sentences))
            }
        } else {
            if capitalization == .uppercase {
                modifier(TextFieldOldViewModifier(capitalization: .allCharacters))
            } else if capitalization == .lowercase {
                modifier(TextFieldOldViewModifier(capitalization: .none))
            } else {
                modifier(TextFieldOldViewModifier(capitalization: .sentences))
            }
        }
    }

    func eraseToAnyView() -> AnyView {
        AnyView(self)
    }
}

@available(iOS 15, *)
struct PresentationViewModifier: ViewModifier {
    func body(content: Content) -> some View {
        content
            .interactiveDismissDisabled()
    }
}

struct PresentationCoverViewModifier: ViewModifier {
    func body(content: Content) -> some View {
        content
    }
}

@available(iOS 15, *)
struct TextFieldViewModifier: ViewModifier {
    var capitalization: TextInputAutocapitalization = .words
    func body(content: Content) -> some View {
        content.textInputAutocapitalization(capitalization)
    }
}

struct TextFieldOldViewModifier: ViewModifier {
    var capitalization: UITextAutocapitalizationType = .words
    func body(content: Content) -> some View {
        content.autocapitalization(capitalization)
    }
}
