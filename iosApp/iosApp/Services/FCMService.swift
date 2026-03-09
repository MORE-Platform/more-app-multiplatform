//
//  FCMService.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 04.04.23.
//  Copyright © 2023 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung
//  Licensed under the Apache 2.0 license with Commons Clause
//  (see https://www.apache.org/licenses/LICENSE-2.0 and
//  https://commonsclause.com/).
//

import FirebaseMessaging
import Foundation
import shared
import UserNotifications

class FCMService: NSObject {
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
            AppDelegate.shared.notificationManager.storeAndHandleNotification(key: msgId, title: content.title, body: content.body, priority: 1, read: false, completed: false, data: data, displayNotification: false)
        }
        do {
            try await AppDelegate.shared.observationService.scheduleObservationReminder()
        } catch {
            Napier.e("Error while updating observation reminder: \(error)")
        }
        return [.sound, .badge, .banner]
    }

    @MainActor
    func userNotificationCenter(_ center: UNUserNotificationCenter, didReceive response: UNNotificationResponse) async {
        let content = response.notification.request.content
        let data = content.userInfo.notNilStringDictionary()
        let msgId = data[NotificationManager.companion.MSG_ID] ?? response.notification.request.identifier

        // Use the shared manager to store and handle the notification interaction, including deep link modification.
        AppDelegate.shared.notificationManager.storeAndHandleNotificationInteraction(
            key: msgId,
            title: content.title,
            body: content.body,
            priority: 1,
            read: true,
            completed: false,
            data: data,
        ) { (actionHandler, deepLinkData) in
            if let deepLinkData {
                switch actionHandler {
                case NotificationActionHandler.deeplink:
                    AppDelegate.navigationScreenHandler.openRoute(to: deepLinkData)
                default:
                    break
                }
            }
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

