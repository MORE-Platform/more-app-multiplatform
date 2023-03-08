//
//  ScheduleViewModel.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 07.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import shared

class ScheduleViewModel: ObservableObject {
    
    private let coreModel: CoreScheduleViewModel = CoreScheduleViewModel()
    @Published var schedules: [UInt64 : [ScheduleModel]] = [:]
    @Published var scheduleDates: [UInt64] = []
    
    func loadObservations() {
        coreModel.onScheduleModelListChange { scheduleMap in
            for (key, value) in scheduleMap {
                self.schedules[UInt64(truncating: key)] = value
            }
            self.scheduleDates = Array(self.schedules.keys).sorted()
        }
    }
}
