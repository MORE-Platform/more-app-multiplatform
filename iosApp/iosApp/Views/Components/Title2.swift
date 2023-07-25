//
//  TitleModifier.swift
//  iosApp
//
//  Created by Isabella Aigner on 21.03.2023
//  Copyright © 2023 Redlink GmbH. All rights reserved.
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
