//
//  MoreMainBackgroundView.swift
//  iosApp
//
//  Created by Jan Cortiel on 01.02.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI

struct MoreMainBackgroundView<Content: View>: View {
    var content: () -> Content
    var body: some View {
        ZStack {
            Color.blue.ignoresSafeArea()
            VStack(alignment: .center) {
                HStack{
                    Image(systemName: "heart")
                    Spacer()
                }
                .padding()
                Spacer()
                content().padding()
                Spacer()
            }
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
