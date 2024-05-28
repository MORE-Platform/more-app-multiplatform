//
//  LocalPushNotificationService.swift
//  iosApp
//
//  Created by Jan Cortiel on 12.04.23.
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
import UIKit
import UserNotifications

class LocalPushNotifications: LocalNotificationListener {
    func clearNotifications() {
        UNUserNotificationCenter.current().removeAllDeliveredNotifications()
    }

    func deleteNotificationFromSystem(notificationId: String) {
        UNUserNotificationCenter.current().removeDeliveredNotifications(withIdentifiers: [notificationId])
    }

    func createNewFCMToken(onCompletion: @escaping (String) -> Void) {
        let notificationCenter = UNUserNotificationCenter.current()
        notificationCenter.getNotificationSettings { settings in
            if settings.authorizationStatus == .authorized {
                DispatchQueue.main.async {
                    if !UIApplication.shared.isRegisteredForRemoteNotifications {
                        AppDelegate.registerForNotifications()
                    }
                }
                Messaging.messaging().token { token, error in
                    if let error {
                        print("Error fetching FCM registration token: \(error)")
                    } else if let token {
                        onCompletion(token)
                    }
                }
            }
        }
    }

    func deleteFCMToken() {
        Messaging.messaging().deleteToken { error in
            if let error = error {
                print("Erro rdeleting FCM registration token: \(error)")
            }
        }
    }

    func displayNotification(notification: NotificationSchema) {
        if let title = notification.title, let body = notification.notificationBody {
            requestLocalNotification(identifier: notification.notificationId, title: title, subtitle: body)
        }
    }

    private func requestLocalNotification(identifier: String, title: String, subtitle: String, timeInterval: TimeInterval = 0, repeates: Bool = false) {
        let content = UNMutableNotificationContent()
        content.title = title
        content.subtitle = subtitle
        content.sound = .default

        let adjustedTimeInterval = max(timeInterval, 1)
        let trigger = UNTimeIntervalNotificationTrigger(timeInterval: adjustedTimeInterval, repeats: repeates)

        let request = UNNotificationRequest(identifier: identifier, content: content, trigger: trigger)

        UNUserNotificationCenter.current().add(request) { error in
            if let error = error {
                print("Error adding notification: \(error)")
            } else {
                print("Local Notification requested!")
            }
        }
    }
}
