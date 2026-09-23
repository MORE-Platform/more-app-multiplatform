//
//  PolarUserProfile.swift
//  iosApp
//

import Foundation
import PolarBleSdk

struct PolarUserProfile {

    enum Gender: String, CaseIterable {
        case female, male

        var displayName: String {
            switch self {
            case .female: return "Female"
            case .male: return "Male"
            }
        }

        var polarGender: PolarFirstTimeUseConfig.Gender {
            switch self {
            case .female: return .female
            case .male: return .male
            }
        }
    }

    var gender: Gender
    var age: Int
    var heightCm: Int
    var weightKg: Int

    /// Approximate birth date derived from age (set to Jan 1 of the birth year).
    var birthDate: Date {
        let year = Calendar.current.component(.year, from: Date()) - age
        return Calendar.current.date(from: DateComponents(year: year, month: 1, day: 1)) ?? Date()
    }

    // MARK: - Persistence

    // Storage keys deliberately keep the original "polar360." prefix: renaming them would
    // orphan the profile saved by an existing install and silently fall back to FTU defaults.
    private static let genderKey = "polar360.profile.gender"
    private static let ageKey    = "polar360.profile.age"
    private static let heightKey = "polar360.profile.heightCm"
    private static let weightKey = "polar360.profile.weightKg"

    static func load() -> PolarUserProfile? {
        guard
            let defaults = AppDelegate.appGroupUserDefaults,
            let genderRaw = defaults.string(forKey: genderKey),
            let gender = Gender(rawValue: genderRaw),
            defaults.object(forKey: ageKey) != nil,
            defaults.object(forKey: heightKey) != nil,
            defaults.object(forKey: weightKey) != nil
        else { return nil }

        return PolarUserProfile(
            gender: gender,
            age: defaults.integer(forKey: ageKey),
            heightCm: defaults.integer(forKey: heightKey),
            weightKg: defaults.integer(forKey: weightKey)
        )
    }

    func save() {
        guard let defaults = AppDelegate.appGroupUserDefaults else { return }
        defaults.set(gender.rawValue, forKey: Self.genderKey)
        defaults.set(age, forKey: Self.ageKey)
        defaults.set(heightCm, forKey: Self.heightKey)
        defaults.set(weightKg, forKey: Self.weightKey)
    }
}
