//
//  ObservationTimeDetails.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 03.04.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import Foundation
import SwiftUI

struct ObservationTimeDetails: View {
    var start: Int64
    var end: Int64
    private let stringTable = "ScheduleListView"

    var body: some View {
        HStack {
            Image(systemName: "clock.fill")
            BasicText(text: .constant(String(format: "%@:", String.localizedString(forKey: "timeframe", inTable: stringTable, withComment: "when the observation was started"))))
            Text(String(format: "%@ - %@", start.toDateString(dateFormat: "HH:mm"), end.toDateString(dateFormat: "HH:mm")))
                .foregroundColor(Color.more.secondary)
        }
    }
}
