//
//  MoreMainBackgroundView.swift
//  iosApp
//
//  Created by Jan Cortiel on 01.02.23.
//  Copyright © 2023 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung 
//  Licensed under the Apache 2.0 license with Commons Clause 
//  (see https://www.apache.org/licenses/LICENSE-2.0 and
//  https://commonsclause.com/).
//

import SwiftUI

struct MoreMainBackgroundView<Content: View>: View {
    var contentPadding: CGFloat = 10
    var content: () -> Content
    var body: some View {
        ZStack(alignment: .top) {
            Color.more.mainBackground.ignoresSafeArea()
            VStack(alignment: .center) {
                content()
            }
            .foregroundColor(.more.primary)
            .padding(.horizontal, contentPadding)
        }
        
    }
}

struct MoreMainBackgroundView_Previews: PreviewProvider {
    static var previews: some View {
        MoreMainBackgroundView {
            Text("Hello World")
        }
    }
}
