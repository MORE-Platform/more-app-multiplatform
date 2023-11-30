//
//  NavigationText.swift
//  iosApp
//
//  Created by Jan Cortiel on 15.03.23.
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

struct NavigationText: View {
    var text: String
    var body: some View {
        Text(text)
            .font(.headline)
            .foregroundColor(.more.secondary)
    }
}

struct NavigationText_Previews: PreviewProvider {
    static var previews: some View {
        NavigationText(text: "Hello World")
    }
}
