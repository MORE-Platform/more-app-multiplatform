//
//  ConsentListHeader.swift
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
            SectionHeading(sectionTitle: title)
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
