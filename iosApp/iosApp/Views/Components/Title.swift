//
//  TitleModifier.swift
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

struct Title: View {
    var titleText: String
    var textAlignment: TextAlignment = .leading
    var body: some View {
        Text(titleText)
            .font(.more.title)
            .foregroundColor(.more.primary)
            .fontWeight(.more.title)
            .multilineTextAlignment(textAlignment)
    }
}

struct Title_Preview: PreviewProvider {
    static var previews: some View {
        Title(titleText: "Hello World")
    }
}
