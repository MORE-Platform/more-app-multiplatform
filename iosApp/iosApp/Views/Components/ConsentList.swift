//
//  ConsentList.swift
//  iosApp
//
//  Created by Jan Cortiel on 08.02.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI
import shared

struct ConsentList: View {
    @Binding var permissionModel: PermissionModel
    
    var body: some View {
        ScrollView {
            ForEach(permissionModel.consentInfo, id: \.self) { consentModel in
                ConsentListItem(consentInfo: consentModel, hasPreview: consentModel.title == "Study Consent")
                    .listRowInsets(.moreListStyleEdgeInsets.listItem)
                    .listRowBackground(Color.more.mainBackground)
            }
        }
        .listStyle(.plain)
    }
}

struct ConsentList_Previews: PreviewProvider {
    static var previews: some View {
        ConsentList(permissionModel: .constant(PermissionModel(studyTitle: "Title", studyParticipantInfo: "Info", studyConsentInfo: "study consent", consentInfo: [
            PermissionConsentModel(title: "Study Consent", info: "Consent Info"),
            PermissionConsentModel(title: "Movement", info: "Accelerometer data"),
        ])))
    }
}
