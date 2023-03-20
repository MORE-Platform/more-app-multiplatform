//
//  NavigationText.swift
//  iosApp
//
//  Created by Jan Cortiel on 15.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI

struct NavigationText: View {
    @Binding var text: String
    var body: some View {
        Text(text)
            .font(.headline)
            .foregroundColor(.more.icons)
    }
}

struct NavigationText_Previews: PreviewProvider {
    static var previews: some View {
        NavigationText(text: .constant("Hello World"))
    }
}
