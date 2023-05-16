//
//  Int64Extension.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 08.03.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import Foundation
import shared

extension Int64 {
    func toDateString(dateFormat: String) -> String {
       toDate().formattedString(dateFormat: dateFormat)
    }
    
    func toKotlinLong() -> KotlinLong {
        return KotlinLong(value: self)
    }
    
    func dateWithoutTime() -> String {
        toDateString(dateFormat: "dd.MM.yyyy")
    }
    
    func toDate() -> Date {
        Date(timeIntervalSince1970: TimeInterval(self))
    }
    
    func startOfDate() -> Date {
        Calendar.current.startOfDay(for: toDate())
    }
}
