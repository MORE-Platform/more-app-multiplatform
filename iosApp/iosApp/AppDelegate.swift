//
//  AppDelegate.swift
//  iosApp
//
//  Created by Jan Cortiel on 23.03.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import Foundation
import UIKit
import BackgroundTasks
import shared
import Firebase
import FirebaseMessaging
import FirebaseAnalytics

class AppDelegate: NSObject, UIApplicationDelegate {
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
        NapierProxyKt.napierDebugBuild()
        #endif
        
        FirebaseApp.configure()
        fcmService.register()
        UIApplication.shared.registerForRemoteNotifications()
        
        registerBackgroundTasks()
        
        if let launchOptions, let userInfo = launchOptions[.remoteNotification] as? [AnyHashable: Any] {
            print("Has launchoptions: \(userInfo)")
            AppDelegate.shared.notificationManager.handleNotificationDataAsync(shared: AppDelegate.shared, data: userInfo.notNilStringDictionary())
        }
        
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
        print("Did register for Remote Notifications With Device Token: \(deviceToken)")
    }
    
    private func registerBackgroundTasks() {
        BGTaskScheduler.shared.register(forTaskWithIdentifier: DataUploadBackgroundTask.taskID, using: nil) { task in
            if let task = task as? BGProcessingTask {
                DataUploadBackgroundTask().handleProcessingTask(task: task)
            }
        }
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


