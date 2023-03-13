import SwiftUI
import shared

struct ContentView: View {
    @EnvironmentObject var viewModel: ContentViewModel
    @State var showSettings = false
    var body: some View {
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
                        if #available(iOS 16.0, *) {
                            Button {
                                viewModel.reloadConsent()
                                showSettings = true
                            } label: {
                                Image(systemName: "gearshape.fill")
                            }.navigationDestination(isPresented: $showSettings, destination: {
                                SettingsView(viewModel: viewModel.settingsViewModel, showSettings: $showSettings)
                            })
                        } else {
                            NavigationLink(destination: SettingsView(viewModel: viewModel.settingsViewModel, showSettings: $showSettings), isActive: $showSettings) {
                                Button {
                                    viewModel.reloadConsent()
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
        }
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
            .environmentObject(ContentViewModel())
    }
}
