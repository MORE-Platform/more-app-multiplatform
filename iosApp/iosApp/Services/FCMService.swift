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
        if let msgId = data[NotificationManager.companion.MSG_ID] {
            AppDelegate.shared.notificationManager.storeAndHandleNotification(shared: AppDelegate.shared, key: msgId, title: content.title, body: content.body, priority: 1, read: false, data: data, displayNotification: false)
        }
        return [.sound,.badge, .banner]
    }
    
    @MainActor
    func userNotificationCenter(_ center: UNUserNotificationCenter, didReceive response: UNNotificationResponse) async {
        let data = response.notification.request.content.userInfo.notNilStringDictionary()
        let msgId = data[NotificationManager.companion.MSG_ID]
        if let deepLinkString = data[NotificationManager.companion.DEEP_LINK] {
            if let deepLink = URL(string: deepLinkString) {
                AppDelegate.navigationScreenHandler.openWithDeepLink(url: deepLink, notificationId: msgId)
            }
        } else if let msgId {
            AppDelegate.shared.notificationManager.markNotificationAsRead(notificationId: msgId)
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
