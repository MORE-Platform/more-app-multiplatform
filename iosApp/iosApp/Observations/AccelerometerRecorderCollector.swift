//
//  AccelerometerRecorderCollector.swift
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

import CoreMotion
import Foundation
import shared

/// Platform integration for `BackgroundAccelerometerObservation` (shared Kotlin) - wraps
/// `CMSensorRecorder`, which keeps buffering accelerometer samples on-device while the app is
/// suspended or killed. Recording/reading are otherwise independent of this app's lifecycle;
/// the shared observation decides when to arm and drain the recorder.
final class AccelerometerRecorderCollector: BackgroundAccelerometerCollector {
    private let recorder = CMSensorRecorder()

    var isRecordingAvailable: Bool {
        CMSensorRecorder.isAccelerometerRecordingAvailable()
    }

    func record(durationSeconds: Double) {
        recorder.recordAccelerometer(forDuration: durationSeconds)
        Napier.d("CMSensorRecorder started recording accelerometer data for the next \(durationSeconds)s...")
    }

    func collect(from: KotlinInstant, to: KotlinInstant) async throws -> [ObservationBulkModel] {
        let start = Date(timeIntervalSince1970: TimeInterval(from.epochSeconds))
        let end = Date(timeIntervalSince1970: TimeInterval(to.epochSeconds))
        guard start < end else {
            Napier.w("AccelerometerRecorderCollector::collect - start must be smaller than end! Start: \(start); End: \(end)")
            return []
        }

        guard let sensorData = recorder.accelerometerData(from: start, to: end) else {
            return []
        }
        return sensorData.enumerated().compactMap { index, data -> ObservationBulkModel? in
            guard index % 2 == 0, let accDatum = data as? CMRecordedAccelerometerData else {
                return nil
            }
            let accel = accDatum.acceleration
            let dict = ["x": accel.x, "y": accel.y, "z": accel.z]
            return ObservationBulkModel(
                data: dict,
                timestamp: Int64(accDatum.startDate.timeIntervalSince1970),
                instanceId: nil
            )
        }
    }
}
