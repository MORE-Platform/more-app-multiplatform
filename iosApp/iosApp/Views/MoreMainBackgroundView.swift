//
//  MoreMainBackgroundView.swift
//  iosApp
//
//  Created by Jan Cortiel on 01.02.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI

struct MoreMainBackgroundView<TopBarContent: View, Content: View>: View {
    var content: () -> Content
    var topBarContent: () -> TopBarContent
    var body: some View {
        ZStack {
            Color.ui.mainBackground.ignoresSafeArea()
            VStack(alignment: .center) {
                HStack{
                    Image("more_logo_blue")
                        .resizable()
                        .aspectRatio(contentMode: .fit)
                        .frame(width: 100)
                    Spacer()
                    topBarContent()
                }
                .padding(.vertical, 16)
                .padding(.horizontal, 24)
                Spacer()
                content().padding()
                Spacer()
            }
            .foregroundColor(.ui.main)
        }
    }
}

struct MoreMainBackgroundView_Previews: PreviewProvider {
    static var previews: some View {
        MoreMainBackgroundView {
            Text("Hello World")
        } topBarContent: {
            Text("Hello World")
        }

    }
}
