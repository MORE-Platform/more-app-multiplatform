//
//  MoreMainBackgroundView.swift
//  iosApp
//
//  Created by Jan Cortiel on 01.02.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI

struct MoreMainBackgroundView<TopBarContent: View, Content: View, BackButton: View>: View {
    var content: () -> Content
    var topBarContent: () -> TopBarContent
    var backButton: () -> BackButton
    var body: some View {
        ZStack {
            if #available(iOS 16.0, *) {
                NavigationStack {
                    MoreMainBackground {
                        content()
                    } backButton: {
                        backButton()
                    } topBarContent: {
                        topBarContent()
                    }
                }
                .navigationBarBackButtonHidden(true)
            } else {
                NavigationView {
                    MoreMainBackground {
                        content()
                    } backButton: {
                        backButton()
                    } topBarContent: {
                        topBarContent()
                    }
                }
                .navigationBarBackButtonHidden(true)
            }
        }.background(Color.more.mainBackground)
    }
}

struct MoreMainBackgroundView_Previews: PreviewProvider {
    static var previews: some View {
        MoreMainBackgroundView {
            Text("Hello World")
        } topBarContent: {
            Text("Hello World")
        } backButton: {
            Button {} label: {
                Image(systemName: "chevron_left")
            }
        }
        
    }
}
