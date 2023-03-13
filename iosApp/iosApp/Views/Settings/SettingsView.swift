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
                Button {
                    viewModel.reloadStudyConfig()
                } label: {
                    Text(String.localizedString(forKey: "refresh_study_config", inTable: stringTable, withComment: "button to refresh study configuration"))
                }
                .frame(maxWidth: .infinity)
                .padding()
                .foregroundColor(.more.white)
                .background(Color.more.main)
                .cornerRadius(.moreBorder.cornerRadius)
                
                ConsentList(permissionModel: .constant(contentViewModel.permissionModel))
                    .padding(.top)
                
                Button {
                    viewModel.leaveStudy()
                    contentViewModel.showLoginView()
                    contentViewModel.credentialsDeleted()
                } label: {
                    Text(String.localizedString(forKey: "leave_study", inTable: stringTable, withComment: "button to refresh study configuration"))
                }
                .frame(maxWidth: .infinity)
                .padding()
                .foregroundColor(.more.white)
                .background(Color.more.important)
                .cornerRadius(.moreBorder.cornerRadius)
            }
        } topBarContent: {
            EmptyView()
        } backButton: {
            HStack {
                if #available(iOS 15.0, *) {
                    MoreBackButton(showContent: $showSettings)
                } else {
                    MoreBackButtonIOS14(showContent: $showSettings)
                }
            }
        }
    }
}

struct SettingsView_Previews: PreviewProvider {
    static var previews: some View {
        SettingsView(viewModel: SettingsViewModel(), showSettings: .constant(true))
            .environmentObject(ContentViewModel())
    }
}
