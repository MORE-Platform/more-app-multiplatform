import SwiftUI
import shared

@main
struct iOSApp: App {
    @Environment(\.scenePhase) var scenePhase
    @StateObject var contentViewModel = ContentViewModel()
    var body: some Scene {
		WindowGroup {
			ContentView()
                .environmentObject(contentViewModel)
                .onAppear{
                    NapierProxyKt.napierDebugBuild()
                }
                .onChange(of: scenePhase) { newPhase in
                    AppState.shared.scenePhase = newPhase
                }
		}
	}
}
