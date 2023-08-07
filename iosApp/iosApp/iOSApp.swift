import SwiftUI
import shared
import BackgroundTasks

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate
    @Environment(\.scenePhase) var scenePhase
    @StateObject var contentViewModel = ContentViewModel()
    
    var body: some Scene {
		WindowGroup {
			ContentView(viewModel: contentViewModel)
                .onAppear{
                    
                }
                .onChange(of: scenePhase) { newPhase in
                    switch newPhase {
                    case .background:
                        AppDelegate.shared.appInForeground(boolean: false)
                        appDelegate.scheduleTasks()
                    case .inactive:
                        break
                    case .active:
                        AppDelegate.shared.appInForeground(boolean: true)
                        appDelegate.cancelBackgroundTasks()
                        break
                    default:
                        break
                    }
                }
		}
	}
}
