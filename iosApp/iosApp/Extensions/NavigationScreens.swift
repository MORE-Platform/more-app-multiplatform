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
    case taskDetails = "Task Details"
    case studyDetails = "Study Details"
    case scanQRCode = "Scan QR Code"
    case questionObservation = "Question Observation"
    case dashboardFilter = "Dashboard Filter"
    case notificationFilter = "Notification Filter"
    case pastObservations = "Past Observations"
    case runningObservations = "Running Observations"
    case observationDetails = "Observation Details"
    case withdrawStudy = "Leave Study"
    case withdrawStudyConfirm = "Confirm to leave the study"
}

extension NavigationScreens {
    func localize(useTable table: String, withComment comment: String) -> String {
        return self.rawValue.localize(useTable: table, withComment: comment)
    }
}
