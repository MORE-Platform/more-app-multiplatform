//
//  InfoList.swift
//  iosApp
//
//  Created by Jan Cortiel on 15.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI

struct InfoList: View {
    var body: some View {
        VStack {
            InfoListItem(title: "Study Details", icon: "gear", destination: {
                StudyDetailsView(viewModel: StudyDetailsViewModel())
            })
            InfoListItem(title: "Running Observations", icon: "gear", destination: {
                SettingsView()
            })
            InfoListItem(title: "Completed Observations", icon: "gear", destination: {
                SettingsView()
            })
            InfoListItem(title: "Settings", icon: "gear", destination: {
                SettingsView()
            })
        }
    }
}

struct InfoList_Previews: PreviewProvider {
    static var previews: some View {
        InfoList()
    }
}
