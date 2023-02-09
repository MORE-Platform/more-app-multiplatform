//
//  ConsentListHeader.swift
//  iosApp
//
//  Created by Jan Cortiel on 08.02.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI

struct ConsentListHeader: View {
    let title: String
    @Binding var isOpen: Bool
    var body: some View {
        HStack {
            SectionHeading(sectionTitle: .constant(title))
            Spacer()
            UIToggleFoldViewButton(isOpen: $isOpen)
        }
    }
}

struct ConsentListHeader_Previews: PreviewProvider {
    static var previews: some View {
        ConsentListHeader(title: "Test", isOpen: .constant(false))
    }
}
