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
import Realm

class FCMService: NSObject {
    private let notificationRepository = NotificationRepository()

    func register() {
        UNUserNotificationCenter.current().delegate = self
        Messaging.messaging().delegate = self
    }
}

extension FCMService: MessagingDelegate {
    func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
        print("FCM Token received: \(String(describing: fcmToken))")
        if let fcmToken {
            AppDelegate.shared.notificationManager.doNewFCMToken(token: fcmToken)
        }
    }
}

extension FCMService: UNUserNotificationCenterDelegate {
    
    @MainActor
    func userNotificationCenter(_ center: UNUserNotificationCenter, willPresent notification: UNNotification) async -> UNNotificationPresentationOptions {
        let content = notification.request.content
        let data = content.userInfo.notNilStringDictionary()
        if let msgId = data["MSG_ID"] {
            AppDelegate.shared.notificationManager.storeAndHandleNotification(shared: AppDelegate.shared, key: msgId, title: content.title, body: content.body, priority: 2, read: false, data: data, displayNotification: false)
            
            print("Will present Notification with content \(content)")
            return [.banner]
        }
        return [.sound,.badge, .banner]
    }
    
    @MainActor
    func userNotificationCenter(_ center: UNUserNotificationCenter, didReceive response: UNNotificationResponse) async {
        if response.notification.request.identifier.contains("LOCAL_") {
            let content = response.notification.request.content
            print("User info: \(content.userInfo)")
            let data = content.userInfo.notNilStringDictionary()
            let uuid = data["MSG_ID"] ?? UUID().uuidString
            
            print("Received Notification with content: \(content)")
            
            AppDelegate.shared.notificationManager.storeAndHandleNotification(shared: AppDelegate.shared, key: uuid, title: response.notification.request.content.title, body: response.notification.request.content.body, priority: 2, read: false, data: data, displayNotification: false)
        }
            
    }
}

extension Dictionary where Key == AnyHashable {
    func notNilStringDictionary() -> [String: String] {
        var data = [String: String]()
        
        for (key, value) in self {
            if let value = value as? String {
                data[String(describing: key)] = value
            }
        }
        return data
    }
}
