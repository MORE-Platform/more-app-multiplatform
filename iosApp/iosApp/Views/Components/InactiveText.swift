//
//  InactiveText.swift
//  iosApp
//
//  Created by Jan Cortiel on 08.02.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI

struct InactiveText: View {
    @Binding var text: String
    var body: some View {
        Text(text)
            .font(.moreFont.inactiveText)
            .foregroundColor(.more.inactiveText)
            .lineLimit(1)
    }
}

struct InactiveText_Previews: PreviewProvider {
    static var previews: some View {
        InactiveText(text: .constant("Hello World"))
    }
}
