import SwiftUI
import shared

struct ContentView: View {
    @StateObject var viewModel: ContentViewModel
    var body: some View {
        VStack {
            if viewModel.hasCredentials {
                MainTabView()
                    .environmentObject(viewModel)
            } else {
                MoreMainBackground {
                    VStack {
                        if viewModel.loginViewScreenNr == 0 {
                            if let loginViewModel = viewModel.loginViewModel {
                                LoginView(model: loginViewModel)
                            }
                        } else {
                            if let consentViewModel = viewModel.consentViewModel {
                                ConsentView(viewModel: consentViewModel)
                            }
                        }
                    }
                } topBarContent: {
                    EmptyView()
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
