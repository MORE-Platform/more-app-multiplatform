//
//  AccelerometerObservation.swift
//  iosApp
//
//  Created by Jan Cortiel on 08.03.23.
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
import CoreMotion
import shared

class AccelerometerObservation: Observation_ {
    private let motion = CMMotionManager()
    private var accelerometerFrequency = 1.0/60.0
    
    private var timer: Timer? = nil
    
    init(sensorPermission: Set<String>) {
        super.init(observationType: AccelerometerType(sensorPermissions: sensorPermission))
    }
    
    override func start() -> Bool {
        if motion.isAccelerometerAvailable {
            self.timer = setTimer()
            guard let timer else {
                return false
            }
            self.motion.startAccelerometerUpdates()
            RunLoop.main.add(timer, forMode: .default)
            
            return true
        }
        return false
    }
    
    override func stop(onCompletion: @escaping () -> Void) {
        timer?.invalidate()
        motion.stopAccelerometerUpdates()
        onCompletion()
    }
    
    override func observerErrors() -> Set<String> {
        var errors: Set<String> = []
        if !motion.isAccelerometerAvailable {
            errors.insert("Accelerometer Sensor not available")
        }
        return errors
    }
    
    override func applyObservationConfig(settings: Dictionary<String, Any>){
        
    }
    
    private func setTimer() -> Timer {
        Timer(fire: Date(), interval: accelerometerFrequency, repeats: true, block: { timer in
            if let data = self.motion.accelerometerData {
                let dict = ["x": data.acceleration.x, "y": data.acceleration.y, "z": data.acceleration.z]
                self.storeData(data: dict, timestamp: -1){}
            }
        })
    }
}
