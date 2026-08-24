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
//  Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
//

import FirebaseMessaging
import Foundation
import shared
import UIKit
import UserNotifications

class LocalPushNotifications: LocalNotificationListener {
    private static let notificationCountKey = "notification_count"

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
                        Napier.e("Error fetching FCM registration token: \(error)")
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
                Napier.e("Erro rdeleting FCM registration token: \(error)")
            }
        }
    }

    func displayNotification(notification: NotificationEntity, badgeCount: Int32) {
        if let title = notification.title, let body = notification.notificationBody {
            let content = UNMutableNotificationContent()
            content.title = String(localized: .init(title), bundle: .main)
            content.subtitle = NotificationTextLocalization.shared.localizeToStringDesc(raw: body)?.localized() ?? String(localized: .init(body), bundle: .main)

            if let deepLink = notification.deepLink {
                content.userInfo[NotificationManager.companion.DEEP_LINK] = deepLink
            }

            content.sound = .default
            if let scheduledDate = notification.timestamp?.toInt64().toDate(), Date.now < scheduledDate {
                content.badge = NSNumber(value: 1)

                requestLocalNotification(identifier: notification.notificationId, content: content, on: scheduledDate)
            } else {
                requestLocalNotification(identifier: notification.notificationId, content: content)
            }
        }
    }

    func clearScheduledNotifications(notifications: [NotificationEntity]) {
        let center = UNUserNotificationCenter.current()
        let identifiers = notifications.map { $0.notificationId }

        if identifiers.isEmpty {
            center.getPendingNotificationRequests { requests in
                let allIDs = requests.map { $0.identifier }
                if !allIDs.isEmpty {
                    center.removePendingNotificationRequests(withIdentifiers: allIDs)
                }
                center.removeAllDeliveredNotifications()

                center.getPendingNotificationRequests { remaining in
                    if remaining.isEmpty {
                        Napier.i("All pending notifications cleared")
                    } else {
                        Napier.w("Pending notifications still present after clear: \(remaining.map { $0.identifier })")
                    }
                }
            }
        } else {
            center.removePendingNotificationRequests(withIdentifiers: identifiers)
            center.removeDeliveredNotifications(withIdentifiers: identifiers)

            center.getPendingNotificationRequests { remaining in
                let stillPending = remaining.map { $0.identifier }.filter { identifiers.contains($0) }
                if stillPending.isEmpty {
                    Napier.i("Cleared scheduled notifications: \(identifiers)")
                } else {
                    Napier.w("Some notifications still pending after clear: \(stillPending)")
                }
            }
        }
    }


    func updateBadgeCount(count: Int32) {
        setAppGroupNotificiationCount(Int(count))
        UNUserNotificationCenter.current().setBadgeCount(Int(count)) { error in
            Napier.e(error?.localizedDescription ?? "Error setting badge count")
        }
    }
    
    

    private func requestLocalNotification(identifier: String, content: UNMutableNotificationContent, timeInterval: TimeInterval = 0, repeats: Bool = false) {

        let adjustedTimeInterval = max(timeInterval, 1)
        let trigger = UNTimeIntervalNotificationTrigger(timeInterval: adjustedTimeInterval, repeats: repeats)

        let request = UNNotificationRequest(identifier: identifier, content: content, trigger: trigger)

        UNUserNotificationCenter.current().add(request) { error in
            if let error = error {
                Napier.e("Error adding notification\(identifier): \(error)")
            } else {
                Napier.i("Local Notification \(identifier) requested!")
            }
        }
    }

    private func requestLocalNotification(identifier: String, content: UNMutableNotificationContent, on date: Date, repeats: Bool = false) {
        let components = Calendar.current.dateComponents([.year, .month, .day, .hour, .minute, .second], from: date)
        let trigger = UNCalendarNotificationTrigger(dateMatching: components, repeats: repeats)

        let request = UNNotificationRequest(identifier: identifier, content: content, trigger: trigger)

        UNUserNotificationCenter.current().add(request) { error in
            if let error = error {
                Napier.e("Error adding scheduled notification \(identifier): \(error)")
            } else {
                Napier.i("Scheduled Local Notification \(identifier) for \(date)")
            }
        }
    }
    
    private func setAppGroupNotificiationCount(_ count: Int) {
        AppDelegate.appGroupUserDefaults?.set(count, forKey: LocalPushNotifications.notificationCountKey)
    }
}

