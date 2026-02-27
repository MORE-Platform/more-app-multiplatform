//
//  AppDelegate.swift
//  iosApp
//
//  Created by Jan Cortiel on 23.03.23.
//  Copyright © 2023 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung
//  Licensed under the Apache 2.0 license with Commons Clause
//  (see https://www.apache.org/licenses/LICENSE-2.0 and
//  https://commonsclause.com/).
//

import BackgroundTasks
import FirebaseCore
import FirebaseCrashlyticsSwift
import FirebaseMessaging
import Foundation
import shared
import UIKit

class AppDelegate: NSObject, UIApplicationDelegate {
    static let bundleId = Bundle.main.bundleIdentifier ?? "io.redlink.umm.blendedcare.ios"
    static let appGroup = "group." + bundleId
    static let appGroupUserDefaults = UserDefaults(suiteName: appGroup)
    static let database = DatabaseManagerKt.getRoomDatabase(builder: DatabaseManager_iosKt.getDatabaseBuilder())
    static let repositories = MainRepository(appDatabase: database)
    static let navigationScreenHandler = NavigationModalState(repos: repositories)
    static let polarConnector = PolarConnector()
    static let dataUploadManager = DataUploadManager()
    static let shared: Shared = {
        let dataManager = iOSObservationDataManager(repository: repositories)

        return Shared(
            localNotificationListener: LocalPushNotifications(),
            repositories: repositories,
            sharedStorageRepository: UserDefaultsRepository(),
            observationDataManager: dataManager,
            mainBluetoothConnector: polarConnector,
            observationFactory: IOSObservationFactory(repository: repositories, dataManager: dataManager),
            dataRecorder: IOSDataRecorder(),
            reminderNotificationSchedulingLimit: 30
        )
    }()

    private let fcmService: FCMService = FCMService()

    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil) -> Bool {
        #if DEBUG
            NapierProxyKt.napierDebugBuild(antilog: nil)
        #endif

        FirebaseApp.configure()
        FirebaseConfiguration.shared.setLoggerLevel(.debug)
        fcmService.register()
        AppDelegate.registerForNotifications()

        DataUploadBackgroundTask.setupBackgroundTasks()
        DailyBackgroundTask.setupBackgroundTasks()
        ObservationReminderBackgroundTask.setupBackgroundTasks()
        
        let routes = Set(NavigationScreen.allCases.map { $0.values.navigationLink.route })

        AppDelegate.shared.deeplinkManager.addAvailableDeepLinks(deepLinks: routes)

        return true
    }

    func application(_ application: UIApplication, didReceiveRemoteNotification userInfo: [AnyHashable: Any], fetchCompletionHandler completionHandler: @escaping (UIBackgroundFetchResult) -> Void) {
        print("Notification Received: \(userInfo)")
        AppDelegate.shared.notificationManager.handleNotificationDataAsync(shared: AppDelegate.shared, data: userInfo.notNilStringDictionary())

        completionHandler(.newData)
    }

    func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        print("Did register for Remote Notifications With Device Token: \(String(decoding: deviceToken, as: UTF8.self))")
        Messaging.messaging().apnsToken = deviceToken
    }

    private func application(_ application: UIApplication, didFailToRegisterForRemoteNotificationsWithError error: Error) {
        print("App did fail to register for remote notifications: \(error)")
    }

    func cancelBackgroundTasks() {
        BGTaskScheduler.shared.cancel(taskRequestWithIdentifier: DataUploadBackgroundTask.taskID)
        BGTaskScheduler.shared.cancel(taskRequestWithIdentifier: DailyBackgroundTask.taskID)
        BGTaskScheduler.shared.cancel(taskRequestWithIdentifier: ObservationReminderBackgroundTask.taskID)
    }

    func scheduleTasks() {
        DataUploadBackgroundTask.schedule()
        DailyBackgroundTask.schedule()
        ObservationReminderBackgroundTask.schedule()
    }

    static func registerForNotifications() {
        DispatchQueue.main.async {
            if !UIApplication.shared.isRegisteredForRemoteNotifications {
                UIApplication.shared.registerForRemoteNotifications()
            }
        }
    }
}

extension AppDelegate: MessagingDelegate {
    func messaging(
        _ messaging: Messaging,
        didReceiveRegistrationToken fcmToken: String?
    ) {
        let tokenDict = ["token": fcmToken ?? ""]
        NotificationCenter.default.post(
            name: Notification.Name("FCMToken"),
            object: nil,
            userInfo: tokenDict)
    }
}
