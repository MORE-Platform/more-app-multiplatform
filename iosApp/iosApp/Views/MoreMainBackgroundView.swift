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
    var contentPadding: CGFloat = 0
    var content: () -> Content
    @EnvironmentObject private var contentViewModel: ContentViewModel
    @EnvironmentObject private var navigationModalState: NavigationModalState
    var body: some View {
        ZStack(alignment: .top) {
            Color.more.mainBackground.ignoresSafeArea(.all)
            VStack(alignment: .center) {
                content()
                    .padding(.horizontal, contentPadding)
            }
            .foregroundColor(.more.primary)
        }
        .frame(maxWidth: .infinity)
    }
}

struct MoreMainBackgroundView_Previews: PreviewProvider {
    static var previews: some View {
        MoreMainBackgroundView {
            Text("Hello World")
        }
    }
}
