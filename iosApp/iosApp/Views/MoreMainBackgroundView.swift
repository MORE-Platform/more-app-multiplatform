//
//  MoreMainBackgroundView.swift
//  iosApp
//
//  Created by Jan Cortiel on 01.02.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
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
