//
//  Navigation.swift
//  iosApp
//
//  Created by Jan Cortiel on 13.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI

struct Navigation<Content>: View where Content: View {
    @ViewBuilder var content: () -> Content
    var body: some View {
            if #available(iOS 16, *) {
                NavigationStack(root: content)
            } else {
                NavigationView(content: content)
            }
        }
}

struct Navigation_Previews: PreviewProvider {
    static var previews: some View {
        Navigation {
            Text("Tenovjs")
        }
    }
}
