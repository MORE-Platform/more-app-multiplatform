//
//  Navigation.swift
//  iosApp
//
//  Created by Jan Cortiel on 13.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//  Copyright © 2024 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung
//  Licensed under the Apache 2.0 license
//  (see https://www.apache.org/licenses/LICENSE-2.0).
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
