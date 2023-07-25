//
//  NavigationText.swift
//  iosApp
//
//  Created by Jan Cortiel on 15.03.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
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
