//
//  SectionTitle.swift
//  iosApp
//
//  Created by Jan Cortiel on 06.02.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import SwiftUI

struct SectionHeading: View {
    @Binding var sectionTitle: String
    var font: Font = Font.more.headline
    
    var body: some View {
        Text(sectionTitle)
            .font(font)
    }
}
