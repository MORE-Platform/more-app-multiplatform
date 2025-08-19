//
//  HealthKitView.swift
//  iosApp
//
//  Created by Masek Gergely on 16.06.25.
//  Copyright © 2025 Redlink GmbH. All rights reserved.
//

import SwiftUI
import HealthKitUI
import HealthKit

struct HealthKitView: View {
    @StateObject private var healthHelper = HealthKitHelper()
    @State private var authenticated = false
    let calendar = Calendar(identifier: .gregorian)
    var body: some View {
            VStack {
                Text("Please allow us to access your Health Data").font(.headline).fontWeight(.bold).padding(10).colorScheme(.light).multilineTextAlignment(.center)
                Text("We will only use your data for study purposes and adaptive interventions, and we will never share it with anyone else.").font(.subheadline).padding(10).multilineTextAlignment(.center).font(.caption)
                Button("Allow Data Access") {
                    Task {
                        if #available(iOS 15.0, *) {
                            do {
                                try await healthHelper.authorize()
                                authenticated = true
                            } catch {
                                print("Authorization failed: \(error)")
                                authenticated = false
                            }
                        }
                        else{
                            print("Ios too old")
                        }
                       
                    }
                }
                .disabled(!healthHelper.isAvailable)
                .font(.headline)
                .foregroundColor(.white)
                .padding()
                .frame(maxWidth: .infinity)
                .background(Color.green)
                .cornerRadius(12)
                .padding(.horizontal)
                
                
                if authenticated {
                    Text("HealthKit authorized ✅")
                    Button("Get Data") {
                        if #available(iOS 15.4, *){
                        let calendar = Calendar(identifier: .gregorian)
                        let startDate = calendar.startOfDay(for: Date())
                        let endDate = calendar.date(byAdding: .day, value: 1, to: startDate)
                        let datatype = HKQuantityType(.stepCount)
                        Task {
                                do{
                                    let steps = try await healthHelper.getData(datatype: datatype, daysback: 1)
                                    print(steps)
                                    let steps_2 = try await healthHelper.getSteps()
                                    print(steps_2)
                                }
                            }
                        }
                    }
                    if #available(iOS 15.4, *){
                        
                        Button("Write Steps Sample") {
                            Task {
                                try? await healthHelper.saveSteps(count: 1000, date: Date())
                                print("✅ Steps sample saved")
                            }
                        }
                        .buttonStyle(.borderedProminent)
                        
                        Button("Write Heart Rate Sample") {
                            Task {
                                try? await healthHelper.saveHeartRate(bpm: 72, date: Date())
                                print("✅ Heart rate sample saved")
                            }
                        }
                        .buttonStyle(.borderedProminent)
                        
                        Button("Write Sleep Sample") {
                            Task {
                                let start = Calendar.current.date(byAdding: .hour, value: -8, to: Date())!
                                let end = Date()
                                try? await healthHelper.saveSleep(start: start, end: end)
                                print("✅ Sleep sample saved")
                            }
                        }
                        .buttonStyle(.borderedProminent)
                        
                        Button("Write Workout Sample") {
                            Task {
                                let start = Calendar.current.date(byAdding: .minute, value: -30, to: Date())!
                                let end = Date()
                                try? await healthHelper.saveWorkout(start: start, end: end)
                                print("✅ Workout sample saved")
                            }
                        }
                        .buttonStyle(.borderedProminent)
                    }
                    
                } else {
                    Text("Not authorized yet ❌")
                }
            }
            .onAppear {
                // `healthHelper.isAvailable` is already set in the init()
            }
        }
}
