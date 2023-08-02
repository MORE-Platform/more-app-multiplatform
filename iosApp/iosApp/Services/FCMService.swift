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
        let notificationCenter = UNUserNotificationCenter.current()
        notificationCenter.delegate = self
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
        let uuid = data["msgID"] ?? UUID().uuidString
        
        AppDelegate.shared.notificationManager.storeAndHandleNotification(shared: AppDelegate.shared, key: uuid, title: content.title, body: content.body, priority: 2, read: false, data: data)
        
        print(data)
        return [.sound,.badge, .banner]
    }
    
    @MainActor
    func userNotificationCenter(_ center: UNUserNotificationCenter, didReceive response: UNNotificationResponse) async {
        let content = response.notification.request.content
        print("User info: \(content.userInfo)")
        let data = content.userInfo.notNilStringDictionary()
        let uuid = data["MSG_ID"] ?? UUID().uuidString
        
        AppDelegate.shared.notificationManager.storeAndHandleNotification(shared: AppDelegate.shared, key: uuid, title: response.notification.request.content.title, body: response.notification.request.content.body, priority: 2, read: false, data: data)
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
