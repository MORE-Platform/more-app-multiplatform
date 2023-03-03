//
//  ScheduleListItem.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 03.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI
import shared
import RealmSwift

struct ScheduleListItem: View {
    var schedule: ScheduleSchema
    var observation: ObservationSchema
    let calendar = Calendar.current
    
    var body: some View {
        VStack {
            VStack(alignment: .leading) {
                Text(observation.observationTitle)
                Button {
                } label: {
                    HStack {
                        Text(observation.observationType)
                        Spacer()
                        Image(systemName: "chevron.forward")
                    }
                }.buttonStyle(PlainButtonStyle())
            }
            HStack {
                Image(systemName: "clock.fill")
                Text("Start: 12:00")
                Spacer()
                Text("Active for: ")
                Text("30 min")
            }
            MoreActionButton {
                
            } label: {
                Text("Start Observation")
            }.buttonStyle(PlainButtonStyle())
        }
    }
}
