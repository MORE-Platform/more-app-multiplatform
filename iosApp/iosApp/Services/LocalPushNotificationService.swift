//
//  LocalPushNotificationService.swift
//  iosApp
//
//  Created by Jan Cortiel on 12.04.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import Foundation
import UserNotifications
import shared
import FirebaseMessaging

class LocalPushNotifications: LocalNotificationListener {
    func createNewFCMToken(onCompletion: @escaping (String) -> Void) {
        Messaging.messaging().token { token, error in
            if let error {
                print("Error fetching FCM registration token: \(error)")
            } else if let token {
                onCompletion(token)
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
    
    func requestLocalNotification(identifier: String = UUID().uuidString, title: String, subtitle: String, timeInterval: TimeInterval = 0, repeates: Bool = false) {
        let content = UNMutableNotificationContent()
        content.title = title
        content.subtitle = subtitle
        content.sound = .default
        
        var trigger: UNTimeIntervalNotificationTrigger? = nil
        
        if timeInterval > 0 {
            trigger = UNTimeIntervalNotificationTrigger(timeInterval: timeInterval, repeats: repeates)
        }
        
        let request = UNNotificationRequest(identifier: identifier, content: content, trigger: trigger)
        
        UNUserNotificationCenter.current().add(request)
        print("Local Notification requested!")
    }
}
