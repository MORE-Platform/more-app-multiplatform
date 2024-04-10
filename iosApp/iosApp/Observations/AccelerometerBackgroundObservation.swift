//
//  BackgroundSensorRecorder.swift
//  iosApp
//
//  Created by Jan Cortiel on 20.03.23.
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

class AccelerometerBackgroundObservation: Observation_ {
    private var recordForDurationInSec: Double = 60 * 10
    private let recorder = CMSensorRecorder()
    private var startRecording: Date = Date()
    
    private var timer: Timer?
    private let semaphore = Semaphore()
    private let observationRepository: ObservationRepository = {
        ObservationRepository()
    }()
    
    init(sensorPermissions: Set<String>) {
        super.init(observationType: AccelerometerType(sensorPermissions: sensorPermissions))
    }
    
    override func start() -> Bool {
        if observerAccessible() {
            recorder.recordAccelerometer(forDuration: recordForDurationInSec)
            self.startRecording = Date()
            print("CMSensorRecorder started recording accelerometer data for the next \(recordForDurationInSec)s...")
            DispatchQueue.main.async { [weak self] in
                self?.timer = Timer.scheduledTimer(withTimeInterval: 5, repeats: true) { [weak self] timer in
                    if let self {
                        self.collectData(start: Date(timeIntervalSince1970: TimeInterval(self.lastCollectionTimestamp.epochSeconds)), end: Date()) {
                            if self.startRecording.timeIntervalSince1970 + self.recordForDurationInSec <= Date().timeIntervalSince1970 {
                                timer.invalidate()
                            }
                        }
                    } else {
                        timer.invalidate()
                    }
                }
            }
            
            return true
        }
        return false
    }
    
    override func stop(onCompletion: @escaping () -> Void) {
        timer?.invalidate()
        self.collectData(start: Date(timeIntervalSince1970: TimeInterval(self.lastCollectionTimestamp.epochSeconds)), end: Date(), completion: onCompletion)
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
            DispatchQueue.global(qos: .background).async {
                if let sensorData = self.recorder.accelerometerData(from: start, to: end) {
                    self.collectionTimestampToNow()
                    let data = sensorData.compactMap { data in
                        if let accDatum = data as? CMRecordedAccelerometerData {
                            let accel = accDatum.acceleration
                            let timestamp = accDatum.startDate.timeIntervalSince1970
                            let dict = ["x": accel.x, "y": accel.y, "z": accel.z]
                            return ObservationBulkModel(data: dict, timestamp: Int64(timestamp))
                        }
                        return nil
                    }
                    self.storeData(data: data) {
                        completion()
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
