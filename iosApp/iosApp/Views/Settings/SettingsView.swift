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
    @StateObject var viewModel: SettingsViewModel = SettingsViewModel()
    private let navigationStrings = "Navigation"
    
    var body: some View {
        MoreMainBackgroundView {
            VStack(alignment: .leading) {
                Text(String.localizedString(forKey: "settings_text", inTable: stringTable, withComment: "information about accepted permissions"))
                    .foregroundColor(.more.secondary)
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
                    //contentViewModel.showLoginView()
                } label: {
                    Text(String.localizedString(forKey: "leave_study", inTable: stringTable, withComment: "button to refresh study configuration"))
                }
            }
        } topBarContent: {
            EmptyView()
        }
        .customNavigationTitle(with: NavigationScreens.settings.localize(useTable: navigationStrings, withComment: "Settings Screen"))
        .navigationBarTitleDisplayMode(.inline)
    }
}

struct SettingsView_Previews: PreviewProvider {
    static var previews: some View {
        SettingsView()
    }
}
