//
//  IOSDataRecorder.swift
//  iosApp
//
//  Created by Jan Cortiel on 20.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import Foundation
import shared

class IOSDataRecorder: DataRecorder {
    private let observationManager = ObservationManager(observationFactory: IOSObservationFactory())
    
    func start(scheduleId: String) {
        observationManager.start(scheduleId: scheduleId) { _ in
            
        }
    }
    
    func pause(scheduleId: String) {
        observationManager.pause(scheduleId: scheduleId)
    }
    
    func stop(scheduleId: String) {
        observationManager.stop(scheduleId: scheduleId)
    }
    
    func stopAll() {
        observationManager.stopAll()
    }
    
    func updateTaskStates() {
        observationManager.updateTaskStates()
    }
    
}
