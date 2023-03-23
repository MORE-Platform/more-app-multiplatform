//
//  BackgroundSensorRecorder.swift
//  iosApp
//
//  Created by Jan Cortiel on 20.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import Foundation
import CoreMotion
import shared

class AccelerometerBackgroundObservation: Observation_ {
    static let ACCELEROMETER_RECORDING_DURATION = "accelerometer_recording_duration"
    
    private var recordForDurationInSec: Double = 60 * 10
    private let recorder = CMSensorRecorder()
    private var startRecording: Date = Date()
    private var lastCollectedDataTimestamp: Date = Date()
    private var timer: Timer?
    
    init(sensorPermissions: Set<String>) {
        super.init(observationType: AccelerometerType(sensorPermissions: sensorPermissions))
    }
    
    
    override func start() -> Bool {
        if observerAccessible() {
            recorder.recordAccelerometer(forDuration: recordForDurationInSec)
            self.startRecording = Date()
            timer = Timer.scheduledTimer(withTimeInterval: 60 * 3.1, repeats: true, block: { timer in
                self.collectNewData()
                if self.startRecording + self.recordForDurationInSec >= Date() {
                    timer.invalidate()
                }
            })
            return true
        }
        return false
    }
    
    override func stop() {
        collectNewData()
        timer?.invalidate()
    }
    
    private func collectNewData() {
        if let sensorData = self.recorder.accelerometerData(from: lastCollectedDataTimestamp <= Date() - TimeInterval(12 * 60) ? lastCollectedDataTimestamp : Date() - TimeInterval(12 * 60), to: Date()) {
            for data in sensorData {
                if let accDatum = data as? CMRecordedAccelerometerData {
                    let accel = accDatum.acceleration
                    let timestamp = accDatum.timestamp
                    let dict = ["x": accel.x, "y": accel.y, "z": accel.z]
                    self.storeData(data: dict, timestamp: timestamp.asTimestamp())
                }
            }
            lastCollectedDataTimestamp = Date()
        }
    }
    
    override func applyObservationConfig(settings: [String : Any]) {
        if let duration = settings[AccelerometerBackgroundObservation.ACCELEROMETER_RECORDING_DURATION] as? Double {
            self.recordForDurationInSec = duration
        }
    }
    
    override func observerAccessible() -> Bool {
        return CMSensorRecorder.isAccelerometerRecordingAvailable() && CMSensorRecorder.authorizationStatus() == .authorized
    }
}
