//
//  HeartRateHealthConnectCollector.swift
//  iosApp
//
//  Licensed under the Apache 2.0 license with Commons Clause
//  (see https://www.apache.org/licenses/LICENSE-2.0 and
//  https://commonsclause.com/).
//

import Foundation
import HealthKit
import shared

class HeartRateHealthConnectCollector: HealthConnectCollector {
    let permissionGroup: String = ConstantsKt.HEALTH_COLLECTOR_GROUP
    
    private let manager = HealthKitManager.shared

    var dataType: HealthConnectDataType { .heartRate }
    var permissionKey: String { dataType.subTypeValue }

    func permissionState() async throws -> PermissionApprovalState {
        manager.permissionState(for: .heartRate)
    }

    func requestPermission() async throws {
        try await manager.requestPermissions(for: HealthKitManager.metrics(for: dataType))
    }

    func collect(from: KotlinInstant, to: KotlinInstant) async throws -> [HealthConnectSample] {
        let samples = try await manager.samples(
            for: .heartRate,
            from: Date(timeIntervalSince1970: TimeInterval(from.epochSeconds)),
            to: Date(timeIntervalSince1970: TimeInterval(to.epochSeconds))
        )
        return samples.map { sample in
            let bpm = Int32(sample.quantity.doubleValue(for: HealthKitManager.Metric.heartRate.unit).rounded())
            let timestamp = KotlinInstant.companion.fromEpochMilliseconds(
                epochMilliseconds: Int64(sample.startDate.timeIntervalSince1970 * 1000)
            )
            return HealthConnectSample.HeartRate(
                timestamp: timestamp,
                bpm: bpm,
                device: sample.device?.name ?? sample.device?.model,
                sourceApp: sample.sourceRevision.source.bundleIdentifier
            )
        }
    }

    func collectDistanceInMeters(from: KotlinInstant, to: KotlinInstant) async throws -> KotlinDouble? {
        nil
    }

    func hasUnrequestedBonusPermission() async throws -> KotlinBoolean {
        KotlinBoolean(bool: false)
    }
}
