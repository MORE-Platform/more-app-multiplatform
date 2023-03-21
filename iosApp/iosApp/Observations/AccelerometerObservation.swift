//
//  AccelerometerObservation.swift
//  iosApp
//
//  Created by Jan Cortiel on 08.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import Foundation
import CoreMotion
import shared

class AccelerometerObservation: Observation_ {
    private let motion = CMMotionManager()
    private var accelerometerFrequency = 1.0/60.0
    
    private var timer: Timer? = nil
    
    init(sensorPermission: Set<String>) {
        super.init(observationTypeImpl: AccelerometerType(sensorPermissions: sensorPermission))
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
    
    override func stop() {
        timer?.invalidate()
        motion.stopAccelerometerUpdates()
    }
    
    override func observerAccessible() -> Bool {
        motion.isAccelerometerAvailable
    }
    
    override func applyObservationConfig(settings: Dictionary<String, Any>){
        
    }
    
    private func setTimer() -> Timer {
        Timer(fire: Date(), interval: accelerometerFrequency, repeats: true, block: { timer in
            if let data = self.motion.accelerometerData {
                let dict = ["x": data.acceleration.x, "y": data.acceleration.y, "z": data.acceleration.z]
                self.storeData(data: dict)
            }
        })
    }
}
