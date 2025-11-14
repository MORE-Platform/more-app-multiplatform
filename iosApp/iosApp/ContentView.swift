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
struct ContentView: View {
    @StateObject var viewModel: ContentViewModel
    @StateObject private var navigationModalState = AppDelegate.navigationScreenHandler
    var body: some View {
        ZStack {
            MoreMainBackgroundView() {
                VStack {
                    if viewModel.hasCredentials {
                        if !navigationModalState.mayChangeViewStructure() {
                            if navigationModalState.studyIsUpdating {
                                StudyUpdateView()
                                    .padding(.horizontal, navigationModalState.horizontalContentPadding)
                            } else if navigationModalState.currentStudyState == StudyState.paused {
                                StudyPausedView()
                                    .padding(.horizontal, navigationModalState.horizontalContentPadding)
                            } else if navigationModalState.currentStudyState == StudyState.closed {
                                StudyClosedView(viewModel: viewModel)
                                    .padding(.horizontal, navigationModalState.horizontalContentPadding)
                            }
                        } else {
                            MainTabView()
                                .sheet(isPresented: $viewModel.showBleView) {
                                    MoreMainBackgroundView(contentPadding: navigationModalState.horizontalContentPadding) {
                                        BluetoothConnectionView(viewModel: viewModel.bluetoothViewModel, viewOpen: $viewModel.showBleView, showAsSeparateView: true)
                                    }
                                }
                        }
                    } else {
                        VStack {
                            if viewModel.loginViewScreenNr == 0 {
                                LoginView(model: viewModel.loginViewModel)
                                    .onAppear {
                                        navigationModalState.clearViews()
                                        navigationModalState.tagState = 0
                                    }
                            } else {
                                ConsentView(viewModel: viewModel.consentViewModel)
                            }
                        }
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

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView(viewModel: ContentViewModel())
    }
}
