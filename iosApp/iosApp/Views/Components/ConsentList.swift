//
//  ConsentList.swift
//  iosApp
//
//  Created by Jan Cortiel on 08.02.23.
//  Copyright © 2023 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung 
//  Licensed under the Apache 2.0 license with Commons Clause 
//  (see https://www.apache.org/licenses/LICENSE-2.0 and
//  https://commonsclause.com/).
//

import SwiftUI
import shared

struct ConsentList: View {
    var permissionModel: PermissionModel
    
    var body: some View {
        ScrollView {
            ForEach(permissionModel.consentInfo, id: \.self) { consentModel in
                ConsentListItem(consentInfo: consentModel, hasPreview: consentModel.title == "Consent")
                    .listRowInsets(.moreListStyleEdgeInsets.listItem)
                    .listRowBackground(Color.more.mainBackground)
            }
        }
        .listStyle(.plain)
    }
}

struct ConsentList_Previews: PreviewProvider {
    static var previews: some View {
        ConsentList(permissionModel: PermissionModel(studyTitle: "Title", studyParticipantInfo: "Info", studyConsentInfo: "study consent", consentInfo: [
            PermissionConsentModel(title: "Study Consent", info: "Consent Info"),
            PermissionConsentModel(title: "Movement", info: "Accelerometer data"),
        ]))
    }
}
