import SwiftUI
import shared

et@main
struct iOSApp: App {
    @StateObject var contentViewModel = ContentViewModel()
    var body: some Scene {
		WindowGroup {
			ContentView()
                .environmentObject(contentViewModel)
                .onAppear{
                    NapierProxyKt.napierDebugBuild()
                }
		}
	}
}
