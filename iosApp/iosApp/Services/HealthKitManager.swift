//
//  HealthKitManager.swift
//  iosApp
//
//  Licensed under the Apache 2.0 license with Commons Clause
//  (see https://www.apache.org/licenses/LICENSE-2.0 and
//  https://commonsclause.com/).
//

import Foundation
import HealthKit
import shared

/// Singleton bridging Apple HealthKit to the shared `HealthConnectCollector` contract. The
/// consent/permission flow requests every needed metric in one system prompt via
/// `requestPermissions(for:)`; `permissionState(for:)`/`requestPermission(for:)` allow
/// checking/requesting a single metric, e.g. when a collector is registered later on.
final class HealthKitManager {
    static let shared = HealthKitManager()

    /// One case per Health Connect subtype backed by HealthKit; mirrors `HealthConnectDataType`.
    /// Adding a new metric only needs a new case here plus its `quantityTypeIdentifier`/`unit`.
    enum Metric: CaseIterable {
        case heartRate
        case steps
        case distanceWalkingRunning

        var quantityTypeIdentifier: HKQuantityTypeIdentifier {
            switch self {
            case .heartRate: return .heartRate
            case .steps: return .stepCount
            case .distanceWalkingRunning: return .distanceWalkingRunning
            }
        }

        var quantityType: HKQuantityType {
            HKQuantityType.quantityType(forIdentifier: quantityTypeIdentifier)!
        }

        var unit: HKUnit {
            switch self {
            case .heartRate: return HKUnit.count().unitDivided(by: .minute())
            case .steps: return .count()
            case .distanceWalkingRunning: return .meter()
            }
        }
    }

    private let healthStore = HKHealthStore()

    private init() {}

    /// Every HealthKit metric a Health Connect subtype needs, including bonus fields requested in
    /// the same system prompt (steps also reads distance for the daily aggregate). Single source
    /// of truth so the permission-request path and each collector's own `requestPermission()`
    /// cannot diverge.
    static func metrics(for dataType: HealthConnectDataType) -> Set<Metric> {
        switch dataType {
        case .heartRate: return [.heartRate]
        case .steps: return [.steps, .distanceWalkingRunning]
        default: return []
        }
    }

    var isHealthDataAvailable: Bool { HKHealthStore.isHealthDataAvailable() }

    /// HealthKit never reveals whether a *read-only* request was actually granted or denied
    /// (`authorizationStatus` only ever returns `.notDetermined` or `.sharingDenied` for read
    /// types, by design, to keep apps from inferring the presence of health data). So the only
    /// signal available here is whether the user has been asked at all; once asked, treat it as
    /// usable and let `samples(for:)` come back empty if access was actually denied.
    func permissionState(for metric: Metric) -> PermissionApprovalState {
        guard isHealthDataAvailable else { return .declined }
        switch healthStore.authorizationStatus(for: metric.quantityType) {
        case .notDetermined: return .notSet
        default: return .granted
        }
    }

    /// Requests read access for a single metric.
    func requestPermission(for metric: Metric) async throws {
        try await requestPermissions(for: [metric])
    }

    /// Requests read access for a set of metrics in one system prompt.
    func requestPermissions(for metrics: Set<Metric>) async throws {
        guard isHealthDataAvailable, !metrics.isEmpty else { return }
        let types = Set(metrics.map(\.quantityType)) as Set<HKObjectType>
        try await healthStore.requestAuthorization(toShare: [], read: types)
    }

    /// Generic poll entry point: any registered metric's samples within `[from, to)`. Every
    /// `HealthConnectCollector.collect` on iOS delegates to this and transforms the result.
    func samples(for metric: Metric, from: Date, to: Date) async throws -> [HKQuantitySample] {
        guard isHealthDataAvailable else { return [] }
        let predicate = HKQuery.predicateForSamples(withStart: from, end: to, options: .strictStartDate)
        return try await withCheckedThrowingContinuation { continuation in
            let query = HKSampleQuery(
                sampleType: metric.quantityType,
                predicate: predicate,
                limit: HKObjectQueryNoLimit,
                sortDescriptors: [NSSortDescriptor(key: HKSampleSortIdentifierStartDate, ascending: true)]
            ) { _, samples, error in
                if let error {
                    continuation.resume(throwing: error)
                } else {
                    continuation.resume(returning: (samples as? [HKQuantitySample]) ?? [])
                }
            }
            healthStore.execute(query)
        }
    }
}
