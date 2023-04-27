//
//  RealmInstantExtension.swift
//  More
//
//  Created by Julia Mayrhauser on 27.04.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import Foundation
import RealmSwift

extension RealmInstant {
    func toEpochMilliseconds() -> Int64 {
        return self.epochSeconds * 1000
    }
}
