//
//  SectionTitle.swift
//  iosApp
//
//  Created by Jan Cortiel on 06.02.23.
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

struct SectionHeading: View {
    var sectionTitle: String
    var font: Font = Font.more.headline
    var showAllText = false
    
    var body: some View {
        if showAllText {
            Text(sectionTitle)
                .font(font)
                .fixedSize(horizontal: false, vertical: true)
        } else {
            Text(sectionTitle)
                .font(font)
        }
    }
}
