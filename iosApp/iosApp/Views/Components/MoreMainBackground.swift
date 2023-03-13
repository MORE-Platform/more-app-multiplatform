//
//  MoreMainBackground.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 13.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI

struct MoreMainBackground<TopBarContent: View, Content: View, BackButton: View>: View {
    var content: () -> Content
    var backButton: () -> BackButton
    var topBarContent: () -> TopBarContent
    var body: some View {
        ZStack {
            Color.more.mainBackground.ignoresSafeArea()
            VStack(alignment: .center) {
                content()
                Spacer()
            }
            .foregroundColor(.more.main)
            .padding(.horizontal, 24)
        }
        .toolbar {
            ToolbarItem(placement: .navigationBarLeading) {
                backButton()
            }
            ToolbarItem(placement: .principal) {
                Image("more_logo_blue")
                    .resizable()
                    .aspectRatio(contentMode: .fit)
                    .frame(width: 100)
            }
            ToolbarItem(placement: .navigationBarTrailing) {
                topBarContent()
            }
        }
    }
}
