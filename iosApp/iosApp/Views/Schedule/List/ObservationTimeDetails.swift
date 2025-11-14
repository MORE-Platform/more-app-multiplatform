//
//  ObservationTimeDetails.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 03.04.23.
//  Copyright © 2023 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung 
//  Licensed under the Apache 2.0 license with Commons Clause 
//  (see https://www.apache.org/licenses/LICENSE-2.0 and
//  https://commonsclause.com/).
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
            BasicText(text: String(format: "%@:", String.localize(forKey: "timeframe", withComment: "when the observation was started", inTable: stringTable)))
            Text(String(format: "%@ - %@", start.toDateString(dateFormat: "HH:mm"), end.toDateString(dateFormat: "HH:mm")))
                .foregroundColor(Color.more.secondary)
        }
    }
}
