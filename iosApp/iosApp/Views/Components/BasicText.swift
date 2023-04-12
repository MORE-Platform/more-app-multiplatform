//
//  BasicText.swift
//  iosApp
//
//  Created by Jan Cortiel on 07.02.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import SwiftUI

struct BasicText: View {
    @Binding var text: String
    var color: Color = Color.more.primary
    var font: Font = Font.body
    var body: some View {
        Text(text)
            .foregroundColor(color)
            .font(font)
    }
}

struct BasicText_Previews: PreviewProvider {
    static var previews: some View {
        BasicText(text: .constant("Hello World"))
    }
}
