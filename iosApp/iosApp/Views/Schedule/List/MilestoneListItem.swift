//
//  MilestoneListItem.swift
//  iosApp
//
//  Copyright © 2023 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung
//  Licensed under the Apache 2.0 license with Commons Clause
//  (see https://www.apache.org/licenses/LICENSE-2.0 and
//  https://commonsclause.com/).
//

import shared
import SwiftUI

struct MilestoneSection: View {
    var milestones: [MilestoneEntity]

    var body: some View {
        VStack(alignment: .leading) {
            BasicText(text: String(localized: "milestones"), color: Color.more.primaryDark)
                .font(Font.more.headline)
            Divider()
            ForEach(milestones, id: \.participantMilestoneId) { milestone in
                MilestoneListItem(milestone: milestone)
                Divider()
            }
        }
        .padding(.bottom)
    }
}

struct MilestoneListItem: View {
    var milestone: MilestoneEntity

    var body: some View {
        HStack {
            Image(systemName: "rosette")
                .foregroundColor(Color.more.primary)
            BasicText(text: milestone.name)
                .font(Font.more.headline)
                .foregroundColor(Color.more.primary)
            Spacer()
            Text(milestone.dateTime.toDateString(dateFormat: "dd.MM.yyyy"))
                .foregroundColor(Color.more.secondary)
        }
        .padding(.vertical, 8)
    }
}
