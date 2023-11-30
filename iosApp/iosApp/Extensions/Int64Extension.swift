//
//  Int64Extension.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 08.03.23.
//  Copyright © 2023 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung 
//  Licensed under the Apache 2.0 license with Commons Clause 
//  (see https://www.apache.org/licenses/LICENSE-2.0 and
//  https://commonsclause.com/).
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
