//
//  ConsentListHeader.swift
//  iosApp
//
//  Created by Jan Cortiel on 08.02.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import SwiftUI

struct ConsentListHeader: View {
    let title: String
    @Binding var hasCheck: Bool
    @Binding var isOpen: Bool
    var body: some View {
        HStack {
            if hasCheck {
                Image(systemName: "checkmark")
                    .foregroundColor(.more.approved)
            }
            SectionHeading(sectionTitle: .constant(title))
            Spacer()
            UIToggleFoldViewButton(isOpen: $isOpen)
        }
    }
}

struct ConsentListHeader_Previews: PreviewProvider {
    static var previews: some View {
        ConsentListHeader(title: "Test", hasCheck: .constant(true), isOpen: .constant(false))
    }
}
