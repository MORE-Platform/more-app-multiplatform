//
//  Polar360ProfileFormView.swift
//  iosApp
//

import SwiftUI

struct Polar360ProfileFormView: View {
    let onComplete: () -> Void

    @State private var gender: Polar360UserProfile.Gender = .female
    @State private var ageText: String = "30"
    @State private var heightText: String = "170"
    @State private var weightText: String = "70"

    private var age: Int? { Int(ageText).flatMap { (1...120).contains($0) ? $0 : nil } }
    private var heightCm: Int? { Int(heightText).flatMap { (50...250).contains($0) ? $0 : nil } }
    private var weightKg: Int? { Int(weightText).flatMap { (20...300).contains($0) ? $0 : nil } }
    private var isValid: Bool { age != nil && heightCm != nil && weightKg != nil }

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Title2(titleText: "Polar 360 Setup")
                .padding(.bottom, 8)

            Text("Please provide your physical information for accurate sensor calibration.")
                .font(.subheadline)
                .foregroundColor(.secondary)
                .padding(.bottom, 30)

            ScrollView {
                VStack(alignment: .leading, spacing: 24) {
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Gender")
                            .font(.headline)
                        Picker("Gender", selection: $gender) {
                            ForEach(Polar360UserProfile.Gender.allCases, id: \.self) { g in
                                Text(g.displayName).tag(g)
                            }
                        }
                        .pickerStyle(.segmented)
                    }

                    VStack(alignment: .leading, spacing: 4) {
                        Text("Age (years)")
                            .font(.headline)
                        TextField("e.g. 30", text: $ageText)
                            .keyboardType(.numberPad)
                            .textFieldStyle(.roundedBorder)
                            .overlay(
                                RoundedRectangle(cornerRadius: 6)
                                    .stroke(!ageText.isEmpty && age == nil ? Color.red : Color.clear, lineWidth: 1)
                            )
                    }

                    VStack(alignment: .leading, spacing: 4) {
                        Text("Height (cm)")
                            .font(.headline)
                        TextField("e.g. 170", text: $heightText)
                            .keyboardType(.numberPad)
                            .textFieldStyle(.roundedBorder)
                            .overlay(
                                RoundedRectangle(cornerRadius: 6)
                                    .stroke(!heightText.isEmpty && heightCm == nil ? Color.red : Color.clear, lineWidth: 1)
                            )
                    }

                    VStack(alignment: .leading, spacing: 4) {
                        Text("Weight (kg)")
                            .font(.headline)
                        TextField("e.g. 70", text: $weightText)
                            .keyboardType(.numberPad)
                            .textFieldStyle(.roundedBorder)
                            .overlay(
                                RoundedRectangle(cornerRadius: 6)
                                    .stroke(!weightText.isEmpty && weightKg == nil ? Color.red : Color.clear, lineWidth: 1)
                            )
                    }
                }
            }

            Spacer()

            MoreActionButton(disabled: .constant(!isValid)) {
                Polar360UserProfile(gender: gender, age: age!, heightCm: heightCm!, weightKg: weightKg!).save()
                onComplete()
            } label: {
                Text("Continue")
            }
        }
        .padding(24)
    }
}
