//
//  iOSObservationDataManager.swift
//  iosApp
//
//  Created by Jan Cortiel on 20.03.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import Foundation
import shared

class iOSObservationDataManager: ObservationDataManager {
    
    override func sendData(onCompletion: @escaping (KotlinBoolean) -> Void) {
        AppDelegate.dataUploadManager.uploadData { onCompletion(KotlinBoolean(bool: $0)) }
    }
}
