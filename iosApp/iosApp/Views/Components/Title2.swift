//
//  TitleModifier.swift
//  iosApp
//
//  Created by Isabella Aigner on 21.03.2023
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

struct Title2: View {
    var titleText: String
    var color: Color = Color.more.primary
    var textAlignment: TextAlignment = .leading
    var body: some View {
        Text(titleText)
            .font(.more.title2)
            .foregroundColor(color)
            .fontWeight(.more.title)
            .multilineTextAlignment(textAlignment)
    }
}

struct Title2_Preview: PreviewProvider {
    static var previews: some View {
        Title(titleText: "Hello World")
    }
}
