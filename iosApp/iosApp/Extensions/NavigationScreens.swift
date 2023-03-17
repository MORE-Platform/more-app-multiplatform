//
//  NavigationScreens.swift
//  iosApp
//
//  Created by Jan Cortiel on 15.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import Foundation

enum NavigationScreens: String {
    case dashboard = "Dashboard"
    case notifications = "Notifications"
    case info = "Information"
    case settings = "Settings"
}

extension NavigationScreens {
    func localize(useTable table: String, withComment comment: String) -> String {
        return self.rawValue.localize(useTable: table, withComment: comment)
    }
}
