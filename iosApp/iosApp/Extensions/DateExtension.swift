//
//  DateExtension.swift
//  iosApp
//
//  Created by Jan Cortiel on 29.03.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import Foundation

extension Date {
    func formattedString(dateFormat: String = "dd.MM.yyyy") -> String {
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = dateFormat
        dateFormatter.timeZone = TimeZone.current
        return dateFormatter.string(from: self)
    }
}
