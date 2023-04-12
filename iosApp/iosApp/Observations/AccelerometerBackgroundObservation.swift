//
//  BackgroundSensorRecorder.swift
//  iosApp
//
//  Created by Jan Cortiel on 20.03.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import Foundation
import CoreMotion
import shared

class AccelerometerBackgroundObservation: Observation_ {
    private var recordForDurationInSec: Double = 60 * 10
    private let recorder = CMSensorRecorder()
    private var startRecording: Date = Date()
    private var lastCollectedDataTimestamp: Date = Date()
    private var timer: Timer?
    private let semaphore = Semaphore()
    
    init(sensorPermissions: Set<String>) {
        super.init(observationType: AccelerometerType(sensorPermissions: sensorPermissions))
    }
    
    override func start() -> Bool {
        if observerAccessible() {
            recorder.recordAccelerometer(forDuration: recordForDurationInSec)
            self.startRecording = Date()
            print("CMSensorRecorder started recording accelerometer data for the next \(recordForDurationInSec)s...")
            timer = Timer.scheduledTimer(withTimeInterval: 60 * 3.1, repeats: true, block: { timer in
                self.collectData(start: self.lastCollectedDataTimestamp, end: Date(), completion: {
                    if self.startRecording + self.recordForDurationInSec >= Date() {
                        timer.invalidate()
                    }
                })
            })
            return true
        }
        return false
    }
    
    override func stop(onCompletion: @escaping () -> Void) {
        timer?.invalidate()
        self.collectData(start: lastCollectedDataTimestamp, end: Date(), completion: onCompletion)
    }
    
    override func store(start: Int64, end: Int64, onCompletion: @escaping () -> Void) {
        self.collectData(start: Date(timeIntervalSince1970: TimeInterval(start)), end: Date(timeIntervalSince1970: TimeInterval(end))) {
            print("\(Date()): Data collected")
            super.store(start: start, end: end, onCompletion: {})
            print("\(Date()): Returning from store function")
            onCompletion()
        }
    }
    
    override func applyObservationConfig(settings: [String : Any]) {
        if var start = settings[Observation_.Companion().CONFIG_TASK_START] as? Int64,
           let end = settings[Observation_.Companion().CONFIG_TASK_STOP] as? Int64,
           Date(timeIntervalSince1970: TimeInterval(end)) > Date()
        {
            let startDate = Date(timeIntervalSince1970: TimeInterval(start))
            let endDate = Date(timeIntervalSince1970: TimeInterval(end))
            if startDate < Date() {
                start = Int64(Date().timeIntervalSince1970)
            }
            print("Recording time from \(startDate) to \(endDate); \(Double(end - start))s")
            self.recordForDurationInSec = Double(end - start)
        }
    }
    
    override func observerAccessible() -> Bool {
        return CMSensorRecorder.isAccelerometerRecordingAvailable() && CMSensorRecorder.authorizationStatus() == .authorized
    }
}

extension AccelerometerBackgroundObservation: ObservationCollector {
    func collectData(start: Date, end: Date, completion: @escaping () -> Void) {
        if start < end {
            Task { [weak self] in
                if let self, let sensorData = self.recorder.accelerometerData(from: start, to: end) {
                    lastCollectedDataTimestamp = Date()
                    let data = sensorData.compactMap { data in
                        if let accDatum = data as? CMRecordedAccelerometerData {
                            let accel = accDatum.acceleration
                            let timestamp = accDatum.startDate.timeIntervalSince1970
                            let dict = ["x": accel.x, "y": accel.y, "z": accel.z]
                            return ObservationBulkModel(data: dict, timestamp: Int64(timestamp))
                        }
                        return nil
                    }
                    await MainActor.run {
                        self.storeData(data: data) {
                            print("\(Date()): Data stored")
                            print("\(Date()): Returning from store")
                            completion()
                        }
                    }
                } else {
                    completion()
                }
            }
        } else {
            print("Start must be smaller than end! Start: \(start); End: \(end)")
            completion()
        }
    }
}
