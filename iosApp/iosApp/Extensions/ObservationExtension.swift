//
//  ObservationExtension.swift
//  iosApp
//
//  Created by Jan Cortiel on 23.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import Foundation
import shared

protocol ObservationCollector {
    func collectData(start: Date, end: Date, completion: @escaping () -> Void)
}
