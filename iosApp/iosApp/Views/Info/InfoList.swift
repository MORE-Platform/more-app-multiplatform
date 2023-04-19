//
//  InfoList.swift
//  iosApp
//
//  Created by Jan Cortiel on 15.03.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import SwiftUI

struct InfoList: View {
    @EnvironmentObject var contentViewModel: ContentViewModel
    var body: some View {
        VStack {
            InfoListItem(title: "Study Details", icon: "gear", destination: {
                StudyDetailsView(viewModel: StudyDetailsViewModel())
            })
            InfoListItem(title: "Running Observations", icon: "gear", destination: {
                RunningSchedules()
                    .environmentObject(contentViewModel.scheduleViewModel)
            })
            InfoListItem(title: "Completed Observations", icon: "gear", destination: {
                SettingsView(viewModel: contentViewModel.settingsViewModel)
            })
            InfoListItem(title: "Settings", icon: "gear", destination: {
                SettingsView(viewModel: contentViewModel.settingsViewModel)
            })
        }
    }
}

struct InfoList_Previews: PreviewProvider {
    static var previews: some View {
        InfoList()
    }
}
