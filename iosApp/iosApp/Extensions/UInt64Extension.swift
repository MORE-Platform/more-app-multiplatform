//
//  DateExtension.swift
//  iosApp
//
//  Created by Jan Cortiel on 23.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import Foundation

extension Int64 {
    func toDate() -> Date {
        Date(timeIntervalSince1970: TimeInterval(self / 1000))
    }
}
