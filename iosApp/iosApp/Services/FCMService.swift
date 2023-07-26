//
//  FCMService.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 04.04.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import Firebase
import FirebaseAnalytics
import FirebaseMessaging
import Foundation
import shared

class FCMService: NSObject {
    private let notificationRepository = NotificationRepository()

    func register() {
        let notificationCenter = UNUserNotificationCenter.current()
        notificationCenter.delegate = self
        Messaging.messaging().delegate = self
        
    }

    static func getNotificationToken() {
        Messaging.messaging().token { token, error in
            if let error {
                print("Error fetching FCM registration token: \(error)")
            } else if let token {
                sendToken(fcmToken: token)
            }
        }
    }

    func deleteNotificationToken() {
        Messaging.messaging().deleteToken { error in
            if let error = error {
                print("Erro rdeleting FCM registration token: \(error)")
            }
        }
    }
    
    private static func sendToken(fcmToken: String) {
        Task { @MainActor in
            do {
                print("FCM registration token: \(fcmToken)")
                try await AppDelegate.shared.networkService.sendNotificationToken(token: fcmToken)
            } catch {
                print("Token could not be stored")
            }
        }
    }
}

extension FCMService: MessagingDelegate {
    func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
        print("FCM Token received: \(String(describing: fcmToken))")
    }
}

extension FCMService: UNUserNotificationCenterDelegate {
    func application(
        _ application: UIApplication,
        didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data
    ) {
        Messaging.messaging().apnsToken = deviceToken
    }
    
    
//    func application(_ application: UIApplication, didReceiveRemoteNotification userInfo: [AnyHashable : Any]) async -> UIBackgroundFetchResult {
//        print("didReceiveRemoteNotification: \(userInfo)")
//        return .newData
//    }
    
    @MainActor
    func userNotificationCenter(_ center: UNUserNotificationCenter, willPresent notification: UNNotification) async -> UNNotificationPresentationOptions {
        let userInfo = notification.request.content.userInfo
        let uuid = UUID().uuidString
        let timestamp = NSDate().timeIntervalSince1970
        
        notificationRepository.storeNotification(key: uuid, channelId: nil, title: notification.request.content.title, body: notification.request.content.body, timestamp: Int64(timestamp), priority: 2, read: false, userFacing: true, additionalData: nil)
        
        print(userInfo)
        return [.sound,.badge, .banner, .list]
    }
    
    @MainActor
    func userNotificationCenter(_ center: UNUserNotificationCenter, didReceive response: UNNotificationResponse) async {
        let userInfo = response.notification.request.content.userInfo
        print("User info: \(userInfo)")
    }
}
