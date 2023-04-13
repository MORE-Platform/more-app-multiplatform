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
                    NapierProxyKt.napierDebugBuild()
                }
                .onChange(of: scenePhase) { newPhase in
                    switch newPhase {
                    case .background:
                        appDelegate.scheduleTasks()
                    case .inactive:
                        break
                    case .active:
                        DispatchQueue.main.async {
                            ScheduleRepository().updateTaskStates(observationFactory: IOSObservationFactory())
                        }
                        break
                    default:
                        break
                    }
                }
		}
	}
}
