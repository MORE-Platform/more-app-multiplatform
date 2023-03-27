//
//  Int64Extension.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 08.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import Foundation

extension Int64 {
    func toDateString(dateFormat: String) -> String {
        let date = Date(timeIntervalSince1970: TimeInterval(self / 1000))
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = dateFormat
        dateFormatter.timeZone = TimeZone.current
        return dateFormatter.string(from: date)
    }
    
    func toDate() -> Date {
        return Date(timeIntervalSince1970: TimeInterval(self / 1000))
    }
}
