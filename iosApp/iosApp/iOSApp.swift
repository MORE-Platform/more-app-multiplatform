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
                        if AppDelegate.shared.observationManager.hasRunningTasks() {
                            appDelegate.scheduleTasks()
                        }
                        AppDelegate.shared.appInForeground(boolean: false)
                    case .inactive:
                        break
                    case .active:
                        appDelegate.cancelBackgroundTasks()
                        AppDelegate.shared.appInForeground(boolean: true)
                        break
                    default:
                        break
                    }
                }
		}
	}
}
