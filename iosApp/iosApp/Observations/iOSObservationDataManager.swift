//
//  iOSObservationDataManager.swift
//  iosApp
//
//  Created by Jan Cortiel on 20.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import Foundation
import shared

class iOSObservationDataManager: ObservationDataManager {
    private let dataUploadManager = DataUploadManager()
    override func sendData(onCompletion: @escaping (KotlinBoolean) -> Void) {
//        Task(priority: .utility) {
//            await dataUploadManager.uploadData { onCompletion(KotlinBoolean(bool: $0)) }
//        }
    }
}
