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
    let hasCheckbox: Bool = false
    @State var hasPreview: Bool = false
    
    var body: some View {
        HStack {
            if hasCheckbox {
                VStack {
                    Image(systemName: "checkmark.circle.fill")
                        .foregroundColor(.more.primary)
                    
                }
            }
            VStack(alignment: .leading) {
                ConsentListHeader(title: consentInfo.title, hasCheck: .constant(!hasCheckbox), isOpen: $isOpen)
                	Divider()
                Group {
                    if isOpen {
                        BasicText(text: .constant(consentInfo.info))
                            .padding(.bottom, 20)
                    } else if hasPreview && !isOpen {
                        InactiveText(text: .constant(consentInfo.info))
                            .padding(.bottom, 20)
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
