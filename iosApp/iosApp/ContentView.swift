import SwiftUI
import shared

struct ContentView: View {
    @EnvironmentObject var viewModel: ContentViewModel
    var body: some View {
        MoreMainBackgroundView {
            VStack {
                if viewModel.hasCredentials {
                    DashboardView(viewModel: viewModel.dashboardViewModel)
                } else {
                    if viewModel.loginViewScreenNr == 0 {
                        LoginView(model: viewModel.loginViewModel)
                    } else {
                        ConsentView(viewModel: viewModel.consentViewModel)
                    }
                }
            }
            
        } topBarContent: {
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
