//
//  StepsHealthConnectCollector.swift
//  iosApp
//
//  Licensed under the Apache 2.0 license with Commons Clause
//  (see https://www.apache.org/licenses/LICENSE-2.0 and
//  https://commonsclause.com/).
//

import Foundation
import HealthKit
import shared

final class StepsHealthConnectCollector: HealthConnectCollector {
    let permissionGroup: String = ConstantsKt.HEALTH_COLLECTOR_GROUP
    
    private let manager = HealthKitManager.shared

    let dataType: HealthConnectDataType = .steps
    
    var permissionKey: String { dataType.subTypeValue }

    func permissionState() async throws -> PermissionApprovalState {
        manager.permissionState(for: .steps)
    }

    func requestPermission() async throws {
        // Requested together so distance (a "bonus" field on the daily aggregate, see
        // `collectDistanceInMeters`) is covered by the same system prompt as steps - distance
        // being denied must not block steps collection, so it is not checked in permissionState().
        try await manager.requestPermissions(for: HealthKitManager.metrics(for: dataType))
    }

    func hasUnrequestedBonusPermission() async throws -> KotlinBoolean {
        KotlinBoolean(bool: manager.permissionState(for: .distanceWalkingRunning) == .notSet)
    }

    func collect(
        from: KotlinInstant,
        to: KotlinInstant
    ) async throws -> [HealthConnectSample] {
        let samples = try await manager.samples(
            for: .steps,
            from: Date(timeIntervalSince1970: TimeInterval(from.epochSeconds)),
            to: Date(timeIntervalSince1970: TimeInterval(to.epochSeconds))
        )
        return samples.map { sample in
            let count = Int64(sample.quantity.doubleValue(for: HealthKitManager.Metric.steps.unit).rounded())
            let start = KotlinInstant.companion.fromEpochMilliseconds(
                epochMilliseconds: Int64(sample.startDate.timeIntervalSince1970 * 1000)
            )
            let end = KotlinInstant.companion.fromEpochMilliseconds(
                epochMilliseconds: Int64(sample.endDate.timeIntervalSince1970 * 1000)
            )
            return HealthConnectSample.Steps(
                timestamp: end,
                count: count,
                start: start,
                end: end,
                device: sample.device?.name ?? sample.device?.model,
                sourceApp: sample.sourceRevision.source.bundleIdentifier,
                stepsGoal: nil,
                distanceInMeters: nil
            )
        }
    }

    func collectDistanceInMeters(from: KotlinInstant, to: KotlinInstant) async throws -> KotlinDouble? {
        let samples = try await manager.samples(
            for: .distanceWalkingRunning,
            from: Date(timeIntervalSince1970: TimeInterval(from.epochSeconds)),
            to: Date(timeIntervalSince1970: TimeInterval(to.epochSeconds))
        )
        Napier.d("Received distance samples: \(samples.count)")
        guard !samples.isEmpty else { return nil }
        let total = samples.reduce(0.0) { partial, sample in
            partial + sample.quantity.doubleValue(for: HealthKitManager.Metric.distanceWalkingRunning.unit)
        }
        return KotlinDouble(double: total)
    }
}
