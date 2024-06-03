//
//  NotificationService.swift
//  More-Notification-Service-Extension
//
//  Created by Jan Cortiel on 29.05.24.
//  Copyright © 2024 Redlink GmbH. All rights reserved.
//

import UserNotifications

class NotificationService: UNNotificationServiceExtension {
    private static let appGroup = "group.ac.at.lbg.dhp.more.group"
    private static let notificationCountKey = "notification_count"
    private static let STUDY_UPDATE_NOTIFICATION_KEY = "key"
    private static let STUDY_UPDATE_NOTIFICATION_VALUE = "STUDY_STATE_CHANGED"
    
    var contentHandler: ((UNNotificationContent) -> Void)?
    var bestAttemptContent: UNMutableNotificationContent?
    let defaults = UserDefaults(suiteName: appGroup)

    override func didReceive(_ request: UNNotificationRequest, withContentHandler contentHandler: @escaping (UNNotificationContent) -> Void) {
        self.contentHandler = contentHandler
        bestAttemptContent = (request.content.mutableCopy() as? UNMutableNotificationContent)
        let dict = bestAttemptContent?.userInfo.notNilStringDictionary() ?? [:]
        var notificationCount = defaults?.integer(forKey: NotificationService.notificationCountKey) ?? 0
        
        if let bestAttemptContent {
            bestAttemptContent.badge = (notificationCount + 1) as NSNumber
            
            defaults?.set(notificationCount + 1, forKey: NotificationService.notificationCountKey)
            contentHandler(bestAttemptContent)
        }
    }
    
    override func serviceExtensionTimeWillExpire() {
        // Called just before the extension will be terminated by the system.
        // Use this as an opportunity to deliver your "best attempt" at modified content, otherwise the original push payload will be used.
        if let contentHandler = contentHandler, let bestAttemptContent =  bestAttemptContent {
            contentHandler(bestAttemptContent)
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
