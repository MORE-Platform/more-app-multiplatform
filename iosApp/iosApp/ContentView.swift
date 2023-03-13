import SwiftUI
import shared

struct ContentView: View {
    @EnvironmentObject var viewModel: ContentViewModel
    @State var showSettings = false
    var body: some View {
        ZStack {
            MoreMainBackgroundView {
                VStack {
                    if viewModel.hasCredentials {
                        if showSettings {
                            EmptyView()
                        }
                        else {
                            DashboardView(dashboardViewModel: viewModel.dashboardViewModel)
                        }
                    } else {
                        if viewModel.loginViewScreenNr == 0 {
                            LoginView(model: viewModel.loginViewModel)
                        } else {
                            ConsentView(viewModel: viewModel.consentViewModel)
                        }
                    }
                }
            } topBarContent: {
                HStack {
                    if viewModel.hasCredentials {
                        HStack {
                            Button {
                            } label: {
                                Image(systemName: "bell.fill")
                            }
                            .padding(.horizontal)
                            if #available(iOS 16.0, *) {
                                Button {
                                    showSettings = true
                                } label: {
                                    Image(systemName: "gearshape.fill")
                                }.navigationDestination(isPresented: $showSettings, destination: {
                                    SettingsView(viewModel: viewModel.settingsViewModel, showSettings: $showSettings)
                                })
                            } else {
                                NavigationLink(destination: SettingsView(viewModel: viewModel.settingsViewModel, showSettings: $showSettings), isActive: $showSettings) {
                                    Button {
                                        showSettings = true
                                    } label: {
                                        Image(systemName: "gearshape.fill")
                                    }
                                }
                            }
                        }.foregroundColor(Color.more.icons)
                    } else {
                        EmptyView()
                    }
                }
            } backButton: {
                EmptyView()
        MoreMainBackgroundView {
            VStack {
                if viewModel.hasCredentials {
                    DashboardView(dashboardViewModel: viewModel.dashboardViewModel)
                        .onAppear{
                            viewModel.dashboardViewModel.loadStudy()
                            viewModel.dashboardViewModel.scheduleViewModel.loadData()
                        }
                } else {
                    if viewModel.loginViewScreenNr == 0 {
                        LoginView(model: viewModel.loginViewModel)
                    } else {
                        ConsentView(viewModel: viewModel.consentViewModel)
                    }
                }
            }

        } topBarContent: {
            HStack {
                if viewModel.hasCredentials {
                    HStack {
                        Button {
                        } label: {
                            Image(systemName: "bell.fill")
                        }
                        .padding(.horizontal)
                        Button {

                        } label: {
                            Image(systemName: "gearshape.fill")
                        }
                    }.foregroundColor(Color.more.icons)
                } else {
                    EmptyView()
                }
            }
        }
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
            .environmentObject(ContentViewModel())
    }
}
