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
                MoreMainBackgroundView {
                    VStack {
                        if viewModel.loginViewScreenNr == 0 {
                            LoginView(model: viewModel.loginViewModel)
                        } else {
                            ConsentView(viewModel: viewModel.consentViewModel)
                        }
                    }
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
