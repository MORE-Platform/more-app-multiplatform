//
//  FCMService.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 04.04.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import Foundation
import Firebase
import FirebaseMessaging
import FirebaseAnalytics
import shared


class FCMService {
    private static let networkService = NetworkService.create()
    
    static func getNotificationToken() {
        Messaging.messaging().token { token, error in
            if let error = error {
                print("Error fetching FCM registration token: \(error)")
            } else if let token = token {
                print("FCM registration token: \(token)")
                Task { @MainActor in
                    do {
                        try await self.networkService.sendNotificationToken(token: token)
                    } catch {
                        print(error)
                    }
                }
            }
        }
    }
}
