//
//  NavigationScreens.swift
//  iosApp
//
//  Created by Jan Cortiel on 15.03.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import Foundation

enum NavigationScreens: String {
    case dashboard = "Dashboard"
    case notifications = "Notifications"
    case info = "Information"
    case settings = "Settings"
    case taskDetails = "Task Detail"
    case studyDetails = "Study Details"
    case scanQRCode = "Scan QR Code"
    case questionObservation = "Question Observation"
    case dashboardFilter = "Dashboard Filter"
    case completedObservations = "Completed Observations"
    case runningObservations = "Running Observations"
}

extension NavigationScreens {
    func localize(useTable table: String, withComment comment: String) -> String {
        return self.rawValue.localize(useTable: table, withComment: comment)
    }
}
