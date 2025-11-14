//
//  ObservationDataCollector.swift
//  iosApp
//
//  Created by Jan Cortiel on 29.03.23.
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

class ObservationDataCollector {

    func collectData(dataCollected completion: @escaping (Bool) -> Void) {
        print("Collect undone observations")
        AppDelegate.shared.updateTaskStates()
        AppDelegate.shared.observationManager.collectAllData {success in
            AppDelegate.shared.observationDataManager.saveAndSend()
            Timer.scheduledTimer(withTimeInterval: 2.0, repeats: false) { timer in
                completion(success.boolValue)
            }
        }
    }
}
