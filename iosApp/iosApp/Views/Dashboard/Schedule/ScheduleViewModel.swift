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
    
    static func transfromInt64ToDateString(timestamp: Int64, dateFormat: String) -> String {
        let date = Date(timeIntervalSince1970: TimeInterval(timestamp / 1000))
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = dateFormat
        dateFormatter.timeZone = TimeZone.current
        return dateFormatter.string(from: date)
    }
}
