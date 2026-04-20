//
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
import UIKit
struct ContentView: View {
    @ObservedObject var viewModel: ContentViewModel
    @StateObject private var navigationModalState = AppDelegate.navigationScreenHandler
    var body: some View {
        ZStack {
            MoreMainBackgroundView {
                VStack {
                    if viewModel.hasCredentials {
                        CredentialsView(navigationModalState: navigationModalState, viewModel: viewModel)
                    } else if !viewModel.credentialsLoaded || (viewModel.hasCredentials && navigationModalState.currentStudyState == .none) {
                        StudyLoadingView()
                            .padding(.horizontal, navigationModalState.horizontalContentPadding)
                    } else {
                        RegistrationView(navigationModalState: navigationModalState)
                    }
                }
            }
            if let alertDialog = viewModel.alertDialogModel {
                MoreAlertDialog(alertDialogModel: alertDialog)
            }
        }
        .background(Color.more.mainBackground)
        .environmentObject(navigationModalState)
        .environmentObject(viewModel)
    }
}

struct CredentialsView: View {
    @ObservedObject var navigationModalState: NavigationModalState
    @ObservedObject var viewModel: ContentViewModel
    var body: some View {
        VStack {
            if !navigationModalState.mayChangeViewStructure() {
                VStack {
                    if navigationModalState.studyIsUpdating {
                        StudyUpdateView()
                    } else if navigationModalState.studyLoadingError {
                        StudyLoadingErrorView()
                    } else if navigationModalState.currentStudyState == StudyState.paused {
                        StudyPausedView()
                    } else if navigationModalState.currentStudyState == StudyState.closed {
                        StudyClosedView()
                    }
                }
                .padding(.horizontal, navigationModalState.horizontalContentPadding)
            } else {
                MainTabView()
                    .sheet(isPresented: $viewModel.showBleView) {
                        MoreMainBackgroundView(contentPadding: navigationModalState.horizontalContentPadding) {
                            BluetoothConnectionView(viewOpen: $viewModel.showBleView, showAsSeparateView: true)
                        }
                    }
            }
        }
    }
}

struct RegistrationView: View {
    @ObservedObject var navigationModalState: NavigationModalState
    @StateObject private var registration = RegistrationObservable(service: RegistrationService(shared: AppDelegate.shared))
    @State private var showPolar360ProfileForm = false

    private var studyHasPolar360: Bool {
        registration.study?.observations.contains { $0.observationType.contains("polar360observation") } ?? false
    }

    private func acceptConsent() {
        if let uniqueId = UIDevice.current.identifierForVendor?.uuidString {
            registration.service.acceptConsent(uniqueDeviceId: uniqueId)
        }
    }

    var body: some View {
        VStack {
            if showPolar360ProfileForm {
                Polar360ProfileFormView {
                    acceptConsent()
                }
            } else if registration.study != nil {
                ConsentView(registration: registration, onConsentAccepted: {
                    if studyHasPolar360 && Polar360UserProfile.load() == nil {
                        showPolar360ProfileForm = true
                    } else {
                        acceptConsent()
                    }
                })
            } else {
                LoginView(registration: registration)
                    .onAppear {
                        navigationModalState.clearViews()
                        navigationModalState.tagState = 0
                    }
            }
        }
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView(viewModel: ContentViewModel())
    }
}
