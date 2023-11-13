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

import Foundation
import UIKit
import BackgroundTasks
import shared
import Firebase
import FirebaseMessaging
import FirebaseAnalytics

class AppDelegate: NSObject, UIApplicationDelegate {
    static let navigationScreenHandler = NavigationModalState()
    static let polarConnector = PolarConnector()
    static let dataUploadManager = DataUploadManager()
    static let shared: Shared = {
        let dataManager = iOSObservationDataManager()
        
        return Shared(
            localNotificationListener: LocalPushNotifications(),
            sharedStorageRepository: UserDefaultsRepository(),
            observationDataManager: dataManager,
            mainBluetoothConnector: polarConnector,
            observationFactory: IOSObservationFactory(dataManager: dataManager),
            dataRecorder: IOSDataRecorder()
        )
    }()
    
    private let fcmService: FCMService = FCMService()
    
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
        #if DEBUG
        NapierProxyKt.napierDebugBuild(antilog: nil)
        #endif
        
        FirebaseApp.configure()
        FirebaseConfiguration.shared.setLoggerLevel(.debug)
        fcmService.register()
        
        
        registerBackgroundTasks()
        
        return true
    }
    
    func applicationWillTerminate(_ application: UIApplication) {
        
    }
    
    
    
    func application(_ application: UIApplication, didReceiveRemoteNotification userInfo: [AnyHashable : Any]) async -> UIBackgroundFetchResult {
        print("Notification Received: \(userInfo)")
        AppDelegate.shared.notificationManager.handleNotificationDataAsync(shared: AppDelegate.shared, data: userInfo.notNilStringDictionary())
        
        return .newData
    }
    
    func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        print("Did register for Remote Notifications With Device Token: \(String(decoding: deviceToken, as: UTF8.self))")
        Messaging.messaging().apnsToken = deviceToken
        
    }
    
    private func application(_ application: UIApplication, didFailToRegisterForRemoteNotificationsWithError error: Error) {
        print("App did fail to register for remote notifications: \(error)")
    }
    
    private func registerBackgroundTasks() {
        BGTaskScheduler.shared.register(forTaskWithIdentifier: DataUploadBackgroundTask.taskID, using: nil) { task in
            if let task = task as? BGProcessingTask {
                DataUploadBackgroundTask().handleProcessingTask(task: task)
            }
        }
    }
    
    func cancelBackgroundTasks() {
        BGTaskScheduler.shared.cancel(taskRequestWithIdentifier: DataUploadBackgroundTask.taskID)
    }
    
    func scheduleTasks() {
        DataUploadBackgroundTask.schedule()
    }

    static func registerForNotifications() {
        DispatchQueue.main.async {
            UIApplication.shared.registerForRemoteNotifications()
        }
    }
    deinit {
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


