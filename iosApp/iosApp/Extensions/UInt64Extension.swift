//
//  DateExtension.swift
//  iosApp
//
//  Created by Jan Cortiel on 23.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//  Copyright © 2024 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung
//  Licensed under the Apache 2.0 license
//  (see https://www.apache.org/licenses/LICENSE-2.0).
//

import Foundation

extension Int64 {
    func toDate() -> Date {
        Date(timeIntervalSince1970: TimeInterval(self / 1000))
    }
}
