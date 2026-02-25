//

import BackgroundTasks
import shared

//  Copyright © 2023 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung
//  Licensed under the Apache 2.0 license with Commons Clause
//  (see https://www.apache.org/licenses/LICENSE-2.0 and
//  https://commonsclause.com/).
//

import SwiftUI

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate
    @Environment(\.scenePhase) var scenePhase
    @StateObject var contentViewModel = ContentViewModel()

    var body: some Scene {
        WindowGroup {
            ContentView(viewModel: contentViewModel)
            .onAppear {
            }
            .onChange(of: scenePhase) { newPhase in
                switch newPhase {
                case .background:
                    ViewManager.shared.appIsInForeground(state: false)
                    if AppDelegate.shared.credentialRepository.hasCredentialsValue {
                        appDelegate.scheduleTasks()
                    }
                case .inactive:
                    break
                case .active:
                    ViewManager.shared.appIsInForeground(state: true)
                    appDelegate.cancelBackgroundTasks()
                    break
                default:
                    break
                }
            }
        }
    }
}
