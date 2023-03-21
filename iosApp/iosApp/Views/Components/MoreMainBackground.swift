//
//  MoreMainBackground.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 13.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI

struct MoreMainBackground<TopBarContent: View, Content: View>: View {
    var content: () -> Content
    var topBarContent: () -> TopBarContent
    var body: some View {
        ZStack {
            Color.more.mainBackground.ignoresSafeArea()
            VStack(alignment: .center) {
                content()
            }
            .foregroundColor(.more.primary)
            .padding(.horizontal, 10)
        }
    }
}
