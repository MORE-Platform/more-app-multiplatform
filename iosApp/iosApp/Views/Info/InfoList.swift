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
    private let stringTable = "Info"
    var body: some View {
        VStack(spacing: 14) {
            InfoListItem(title: String.localize(forKey: "Study Details", withComment: "Shows detail description of the study and it's observation moduls.", inTable: stringTable), icon: "info.circle.fill", destination: {
                StudyDetailsView(viewModel: StudyDetailsViewModel())
            })
            InfoListItem(title: "Running Observations", icon: "arrow.triangle.2.circlepath", destination: {
                RunningSchedules(scheduleViewModel: contentViewModel.runningViewModel)
            })
            InfoListItem(title: "Past Observations", icon: "checkmark", destination: {
                CompletedSchedules(scheduleViewModel: contentViewModel.completedViewModel)
            })
            InfoListItem(title: String.localize(forKey: "Devices", withComment: "Lists all connected or needed devices.", inTable: stringTable), icon: "applewatch", destination: {
                BluetoothConnectionView(viewModel: contentViewModel.bluetoothViewModel, viewOpen: .constant(false))
            })
            InfoListItem(title: String.localize(forKey: "Settings", withComment: "Shows the settings for the study.", inTable: stringTable), icon: "gearshape.fill", destination: {
                SettingsView(viewModel: contentViewModel.settingsViewModel)
            })
            InfoListItemModal(title: String.localize(forKey: "Leave Study", withComment: "Leave the study for good.", inTable: stringTable), icon: "rectangle.portrait.and.arrow.right", destination: {
                LeaveStudyView(viewModel: contentViewModel.settingsViewModel)
            }, action: {
                contentViewModel.isLeaveStudyOpen = true
            })
        }
    }
}

struct InfoList_Previews: PreviewProvider {
    static var previews: some View {
        InfoList()
    }
}
