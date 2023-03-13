import SwiftUI
import shared

struct ContentView: View {
    @EnvironmentObject var viewModel: ContentViewModel
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
