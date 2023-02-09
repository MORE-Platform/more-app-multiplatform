import SwiftUI
import shared

struct ContentView: View {
    @EnvironmentObject var viewModel: ContentViewModel
	var body: some View {
        if viewModel.hasCredentials {
            EmptyView()
        } else {
            if viewModel.loginViewScreenNr == 0 {
                LoginView(model: viewModel.loginViewModel)
                    .environmentObject(viewModel)
            } else {
                ConsentView(viewModel: viewModel.consentViewModel)
                    .environmentObject(viewModel)
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
