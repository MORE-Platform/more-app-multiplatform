//
//  BasicText.swift
//  iosApp
//
//  Created by Jan Cortiel on 07.02.23.
//  Copyright © 2023 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung
//  Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
//

import SwiftUI

struct BasicText: View {
    var text: String
    var color: Color = Color.more.primary
    var font: Font = Font.body
    var lineLimit: Int? = nil
    var textAlign: TextAlignment = .leading
    var body: some View {
        Text(LocalizedStringKey(text))
            .foregroundColor(color)
            .multilineTextAlignment(textAlign)
            .fixedSize(horizontal: false, vertical: true)
            .font(font)
            .lineLimit(lineLimit)
            .truncationMode(.tail)
    }
}

struct BasicText_Previews: PreviewProvider {
    static var previews: some View {
        BasicText(text: "Hello World")
    }
}
