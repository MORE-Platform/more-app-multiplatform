import shared
import SwiftUI

struct ContentView: View {
    @StateObject var viewModel: ContentViewModel
    var body: some View {
        VStack {
            if viewModel.hasCredentials {
                if viewModel.studyIsUpdating {
                    StudyUpdateView()
                } else if viewModel.currentStudyState == StudyState.paused {
                    StudyPausedView()
                } else if viewModel.currentStudyState == StudyState.closed {
                    StudyClosedView(viewModel: viewModel)
                } else {
                    MainTabView()
                        .environmentObject(viewModel)
                        .sheet(isPresented: $viewModel.showBleView) {
                            BluetoothConnectionView(viewModel: viewModel.bluetoothViewModel, viewOpen: $viewModel.showBleView, showAsSeparateView: true)
                        }
                }
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
