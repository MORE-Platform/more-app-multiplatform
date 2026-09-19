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
//  Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
//

import SwiftUI

struct InfoList: View {
    @EnvironmentObject var contentViewModel: ContentViewModel
    var body: some View {
        VStack(spacing: 14) {
            InfoListItem(title: "Study Details", icon: "info.circle.fill", destination: .studyDetails)
            InfoListItem(title: "Running Observations", icon: "arrow.triangle.2.circlepath", destination: .runningObservations)
            InfoListItem(title: "Past Observations", icon: "checkmark", destination: .pastObservations)
            InfoListItem(title: "Devices", icon: "applewatch", destination: .bluetoothConnections)
            InfoListItem(title: "Settings", icon: "gearshape.fill", destination: .settings)
            InfoListItem(title: "Leave Study", icon: "rectangle.portrait.and.arrow.right", destination: .withdrawStudy)
        }
    }
}

struct InfoList_Previews: PreviewProvider {
    static var previews: some View {
        InfoList()
    }
}
