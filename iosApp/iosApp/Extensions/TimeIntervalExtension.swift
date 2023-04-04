//
//  TimestampExtension.swift
//  iosApp
//
//  Created by Jan Cortiel on 23.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import Foundation

extension TimeInterval {
    func asTimestamp() -> Int64 {
        Int64(self)
    }
}
