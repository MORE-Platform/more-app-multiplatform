//
//  InfoList.swift
//  iosApp
//
//  Created by Jan Cortiel on 15.03.23.
//  Copyright © 2023 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung 
//  Licensed under the Apache 2.0 license with Commons Clause 
//  (see https://www.apache.org/licenses/LICENSE-2.0 and
//  https://commonsclause.com/).
//

import SwiftUI

struct InfoList: View {
    @EnvironmentObject var contentViewModel: ContentViewModel
    private let stringTable = "Info"
    var body: some View {
        VStack(spacing: 14) {
            InfoListItem(title: String.localize(forKey: "Study Details", withComment: "Shows detail description of the study and it's observation moduls.", inTable: stringTable), icon: "info.circle.fill", destination: .studyDetails)
            InfoListItem(title: String.localize(forKey: "Running Observations", withComment: "Shows a detailed list of running observation", inTable: stringTable), icon: "arrow.triangle.2.circlepath", destination: .runningObservations)
            InfoListItem(title: String.localize(forKey: "Past Observations", withComment: "Shows a detailed list of past observations", inTable: stringTable), icon: "checkmark", destination: .pastObservations)
            InfoListItem(title: String.localize(forKey: "Devices", withComment: "Lists all connected or needed devices.", inTable: stringTable), icon: "applewatch", destination: .bluetoothConnections)
            InfoListItem(title: String.localize(forKey: "Settings", withComment: "Shows the settings for the study.", inTable: stringTable), icon: "gearshape.fill", destination: .settings)
            InfoListItem(title: String.localize(forKey: "Leave Study", withComment: "Leave the study for good.", inTable: stringTable), icon: "rectangle.portrait.and.arrow.right", destination: .withdrawStudy)
        }
    }
}

struct InfoList_Previews: PreviewProvider {
    static var previews: some View {
        InfoList()
    }
}
