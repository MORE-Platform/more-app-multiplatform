//

import BackgroundTasks
import SwiftUI
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
                        PolarController.shared.appIsInBackground = true
                        ViewManager.shared.appIsInForeground(state: false)
                        AppDelegate.shared.updateData(appInForeground: false)
                        if AppDelegate.shared.credentialRepository.hasCredentialsValue {
                            appDelegate.scheduleTasks()
                        }
                    case .inactive:
                        break
                    case .active:
                        PolarController.shared.appIsInBackground = false
                        // Observations have completed (or will complete via beginBackgroundTask /
                        // BGAppRefreshTask). Clear persisted IDs so they don't bleed into the
                        // next session.
                        PolarController.shared.clearBackgroundDeviceId()
                        IOSDataRecorder.clearBackgroundScheduleIds()
                        ViewManager.shared.appIsInForeground(state: true)
                        PermissionManager.resetPermissionAlertFlag()
                        AppDelegate.shared.updateData(appInForeground: true)
                        appDelegate.cancelBackgroundTasks()
                        break
                    default:
                        break
                    }
                }
        }
    }
}
