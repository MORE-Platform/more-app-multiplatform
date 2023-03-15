//
//  ConsentListItem.swift
//  iosApp
//
//  Created by Jan Cortiel on 08.02.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI
import shared

struct ConsentListItem: View {
    let consentInfo: PermissionConsentModel
    @State var isOpen: Bool = false
    var body: some View {
        HStack {
            VStack {
                Image(systemName: "checkmark.circle.fill")
                    .foregroundColor(.more.primary)
                
            }
            Divider()
            VStack(alignment: .leading) {
                ConsentListHeader(title: consentInfo.title, isOpen: $isOpen)
                Divider()
                Group {
                    if isOpen {
                        BasicText(text: .constant(consentInfo.info))
                    } else {
                        InactiveText(text: .constant(consentInfo.info))
                    }
                }
            }
        }
    }
}

struct ConsentListItem_Previews: PreviewProvider {
    static var previews: some View {
        ConsentListItem(consentInfo: PermissionConsentModel(title: "Study Consent", info: "Consent text"))
    }
}
