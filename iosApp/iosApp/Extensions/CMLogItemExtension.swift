//
//  CMLogItemExtension.swift
//  iosApp
//
//  Created by Jan Cortiel on 31.03.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import Foundation
import CoreMotion

extension CMLogItem {
    static let bootTime = Date(timeIntervalSinceNow: -ProcessInfo.processInfo.systemUptime)

    func startTime() -> Date {
        return CMLogItem.bootTime.addingTimeInterval(self.timestamp)
    }
}
