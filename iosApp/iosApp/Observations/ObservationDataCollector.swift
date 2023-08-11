//
//  ObservationDataCollector.swift
//  iosApp
//
//  Created by Jan Cortiel on 29.03.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
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
