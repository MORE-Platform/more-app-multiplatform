//
//  BasicText.swift
//  iosApp
//
//  Created by Jan Cortiel on 07.02.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import SwiftUI

struct BasicText: View {
    var text: String
    var color: Color = Color.more.primary
    var font: Font = Font.body
    var lineLimit: Int? = nil
    var textAlign: TextAlignment = .leading
    var body: some View {
        Text(text)
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
