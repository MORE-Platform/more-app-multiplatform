//
//  MoreNavigationView.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 13.03.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import SwiftUI

struct Navigation<Content: View>: View {
    
    var content: () -> Content
    var body: some View {
        ZStack {
            if #available(iOS 16.0, *) {
                NavigationStack {
                    content()
                }
                .tint(.more.primary)
            } else {
                NavigationView {
                    content()
                }
                .navigationViewStyle(.stack)
                .accentColor(.more.primary)
            }
        }.background(Color.more.secondaryLight)
    }
}

@available(iOS 16, *)
struct NavigationTitleViewModifier: ViewModifier {
    var text: String
    
    func body(content: Content) -> some View {
        content
            .navigationTitle(text)
    }
}

struct NavigationBarTitleViewModifier: ViewModifier {
    var text: String
    
    func body(content: Content) -> some View {
        content
            .navigationBarTitle(text)
    }
}

extension View {
    @ViewBuilder
    func customNavigationTitle(with text: String) -> some View {
        if #available(iOS 16, *) {
            self.modifier(NavigationTitleViewModifier(text: text))
        }
        else {
            self.modifier(NavigationBarTitleViewModifier(text: text))
        }
    }
}
