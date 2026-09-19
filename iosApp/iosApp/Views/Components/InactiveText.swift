//
//  InactiveText.swift
//  iosApp
//
//  Created by Jan Cortiel on 08.02.23.
//  Copyright © 2023 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung
//  Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
//

import SwiftUI

struct InactiveText: View {
    var text: String
    var body: some View {
        Text(LocalizedStringKey(text))
            .font(.moreFont.inactiveText)
            .foregroundColor(.more.textInactive)
            .lineLimit(1)
    }
}

struct InactiveText_Previews: PreviewProvider {
    static var previews: some View {
        InactiveText(text: "Hello World")
    }
}
