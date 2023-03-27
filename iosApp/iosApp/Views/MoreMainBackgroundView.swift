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
            Color.more.mainBackground.ignoresSafeArea()
            VStack(alignment: .center) {
                content()
            }
            .foregroundColor(.more.primary)
            .padding(.horizontal, 24)
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
