//
//  iOSObservationDataManager.swift
//  iosApp
//
//  Created by Jan Cortiel on 20.03.23.
//  Copyright © 2023 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung
//  Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
//

import Foundation
import shared

class iOSObservationDataManager: ObservationDataManager {
    override func sendData(immediately: Bool, onCompletion: @escaping (KotlinBoolean) -> Void) {
        AppDelegate.dataUploadManager.uploadData { onCompletion(KotlinBoolean(bool: $0)) }
    }
}
