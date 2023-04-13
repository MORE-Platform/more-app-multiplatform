//
//  LocalPushNotificationService.swift
//  iosApp
//
//  Created by Jan Cortiel on 12.04.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import Foundation
import UserNotifications

class LocalPushNotifications {
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
