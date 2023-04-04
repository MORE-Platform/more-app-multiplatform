import SwiftUI
import shared
import BackgroundTasks

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate
    @Environment(\.scenePhase) var scenePhase
    
    var body: some Scene {
		WindowGroup {
			ContentView()
                .onAppear{
                    NapierProxyKt.napierDebugBuild()
                }
                .onChange(of: scenePhase) { newPhase in
                    AppState.shared.scenePhase = newPhase
                    switch newPhase {
                    case .background:
                        appDelegate.scheduleTasks()
                    case .inactive:
                        break
                    case .active:
                        break
                    default:
                        break
                    }
                }
		}
	}
}
