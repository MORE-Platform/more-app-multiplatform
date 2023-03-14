//
//  SettingsView.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 08.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI
import shared

struct SettingsView: View {
    private let stringTable = "SettingsView"
    @StateObject var viewModel: SettingsViewModel
    @EnvironmentObject var contentViewModel: ContentViewModel
    @Binding var showSettings: Bool
    
    var body: some View {
        MoreMainBackgroundView {
            VStack(alignment: .leading) {
                Title(titleText: .constant(String.localizedString(forKey: "settings_title", inTable: stringTable, withComment: "Settings title")))
                    .padding(.bottom)
                Text(String.localizedString(forKey: "settings_text", inTable: stringTable, withComment: "information about accepted permissions"))
                    .foregroundColor(.more.icons)
                    .padding(.bottom)
                MoreActionButton {
                    viewModel.reloadStudyConfig()
                } label: {
                    Text(String.localizedString(forKey: "refresh_study_config", inTable: stringTable, withComment: "button to refresh study configuration"))
                }
                
                ConsentList(permissionModel: .constant(viewModel.permissionModel))
                    .padding(.top)
                
                MoreActionButton(backgroundColor: Color.more.important) {
                    viewModel.leaveStudy()
                    showSettings = false
                    contentViewModel.showLoginView()
                } label: {
                    Text(String.localizedString(forKey: "leave_study", inTable: stringTable, withComment: "button to refresh study configuration"))
                }
            }
        } topBarContent: {
            EmptyView()
        }
    }
}

struct SettingsView_Previews: PreviewProvider {
    static var previews: some View {
        SettingsView(viewModel: SettingsViewModel(), showSettings: .constant(true))
            .environmentObject(ContentViewModel())
    }
}
