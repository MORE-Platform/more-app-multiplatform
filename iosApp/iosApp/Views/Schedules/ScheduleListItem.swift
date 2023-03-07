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
import Foundation

struct ScheduleListItem: View {
    @State var observationTitle: String
    @State var observationType: String
    @State var scheduleStart: Date
    @State var scheduleEnd: Date
    @State var observation: ObservationSchema
    @State var activeFor: Int
    let calendar = Calendar.current
    private let dateFormatter = DateFormatter()
    private let buttonLabel = "Start Observation"
    
    var body: some View {
        VStack {
            VStack(alignment: .leading) {
                ObservationDetailsButton(observationTitle: observationTitle, observationType: observationType)
            }
            HStack {
                Image(systemName: "clock.fill")
                BasicText(text: .constant(String(format: "Start: %@", dateFormatter.string(from: scheduleStart))))
                Spacer()
                BasicText(text: .constant(String(format: "Active for: %d min", activeFor)))
            }
            MoreActionButton {
                
            } label: {
                HStack {
                    if observationType.lowercased() == "simple-question-observation" {
                        BasicText(text: .constant("Start Questionnaire"))
                    } else {
                        BasicText(text: .constant("Start Observation"))
                    }
                }
            }.buttonStyle(PlainButtonStyle())
        }
    }
}

struct ScheduleListItem_Previews: PreviewProvider {
    static var previews: some View {
        ScheduleListItem(observationTitle: "Test Title", observationType: "Some Type", scheduleStart: Date(), scheduleEnd: Date(), observation: ObservationSchema(), activeFor: 0)
    }
}
