//
//  TitleModifier.swift
//  iosApp
//
//  Created by Jan Cortiel on 06.02.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import SwiftUI

struct Title: View {
    @Binding var titleText: String
    var body: some View {
        Text(titleText)
            .font(.more.title)
            .foregroundColor(.more.primary)
            .fontWeight(.more.title)
    }
}

struct Title_Preview: PreviewProvider {
    static var previews: some View {
        Title(titleText: .constant("Hello World"))
    }
}
