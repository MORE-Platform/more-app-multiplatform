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
            InfoListItem(title: String.localizedString(forKey: "Study Details", inTable: stringTable, withComment: "Shows detail description of the study and it's observation moduls."), icon: "info.circle.fill", destination: {
                StudyDetailsView(viewModel: StudyDetailsViewModel())
            })
            InfoListItem(title: "Running Observations", icon: "arrow.triangle.2.circlepath", destination: {
                RunningSchedules(scheduleViewModel: ScheduleViewModel(observationFactory: contentViewModel.observationFactory, scheduleListType: .running))
            })
            InfoListItem(title: "Past Observations", icon: "checkmark", destination: {
                CompletedSchedules(scheduleViewModel: ScheduleViewModel(observationFactory: contentViewModel.observationFactory, scheduleListType: .completed))
            })
            InfoListItem(title: String.localizedString(forKey: "Devices", inTable: stringTable, withComment: "Lists all connected or needed devices."), icon: "applewatch", destination: {
                BluetoothConnectionView(viewModel: contentViewModel.bluetoothViewModel)
            })
            InfoListItem(title: String.localizedString(forKey: "Settings", inTable: stringTable, withComment: "Shows the settings for the study."), icon: "gearshape.fill", destination: {
                SettingsView(viewModel: contentViewModel.settingsViewModel)
            })
            InfoListItem(title: String.localizedString(forKey: "Leave Study", inTable: stringTable, withComment: "Leave the study for good."), icon: "rectangle.portrait.and.arrow.right", destination: {
                LeaveStudyView(viewModel: contentViewModel.settingsViewModel)
            })
        }
    }
}

struct InfoList_Previews: PreviewProvider {
    static var previews: some View {
        InfoList()
    }
}
