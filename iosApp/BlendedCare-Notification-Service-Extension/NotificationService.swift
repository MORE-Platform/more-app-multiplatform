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

        let storedCount = defaults?.integer(forKey: NotificationService.notificationCountKey) ?? 0

        let payload = bestAttemptContent?.userInfo as? [AnyHashable: Any]
        let serverBadge =
            (payload?["badge"] as? NSNumber)?.intValue
            ?? (payload?["unread_count"] as? NSNumber)?.intValue
            ?? Int((payload?["unread_count"] as? String) ?? "")

        let proposedCount: Int
        if let serverBadge {
            proposedCount = max(storedCount, serverBadge)
        } else {
            proposedCount = storedCount + 1
        }

        let adjusted = max(0, proposedCount)

        if let bestAttemptContent {
            bestAttemptContent.badge = NSNumber(value: adjusted)
            defaults?.set(adjusted, forKey: NotificationService.notificationCountKey)
            contentHandler(bestAttemptContent)
        }
    }

    override func serviceExtensionTimeWillExpire() {
        // Called just before the extension will be terminated by the system.
        // Use this as an opportunity to deliver your "best attempt" at modified content, otherwise the original push payload will be used.
        if let contentHandler = contentHandler, let bestAttemptContent = bestAttemptContent {
            contentHandler(bestAttemptContent)
        }
    }

}
