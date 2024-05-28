//
//  ObservationExtension.swift
//  iosApp
//
//  Created by Jan Cortiel on 23.03.23.
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
import UIKit

protocol ObservationCollector {
    func collectData(start: Date, end: Date, completion: @escaping () -> Void)
}

extension Observation_ {
    func pauseObservation(_ observationType: ObservationType) {
        AppDelegate.shared.observationManager.pauseObservationType(type: observationType.observationType)
    }
}
