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
    @StateObject var viewModel = SettingsViewModel()
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
                
                ConsentList(permissionModel: .constant(PermissionModel(studyTitle: "Title", studyParticipantInfo: "Info", consentInfo: [
                    PermissionConsentModel(title: "Study Consent", info: "Consent Info"),
                    PermissionConsentModel(title: "Movement", info: "Accelerometer data"),
                ])))
                .padding(.top)
                
                Button {} label: {
                    Text(String.localizedString(forKey: "leave_study", inTable: stringTable, withComment: "button to refresh study configuration"))
                }
                .frame(maxWidth: .infinity)
                .padding()
                .foregroundColor(.more.white)
                .background(Color.more.important)
                .cornerRadius(.moreBorder.cornerRadius)
            }
        } topBarContent: {
            Button {
                
            } label: {
                Image(systemName: "xmark")
            }
        }
    }
}

struct SettingsView_Previews: PreviewProvider {
    static var previews: some View {
        SettingsView()
    }
}
