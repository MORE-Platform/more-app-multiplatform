//
//  ViewModifierExtension.swift
//  iosApp
//
//  Created by Jan Cortiel on 15.03.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import SwiftUI

struct AccentModifier: ViewModifier {
    var color: Color
    func body(content: Content) -> some View {
        if #available(iOS 16, *) {
            content.tint(color)
        } else {
            content.accentColor(color)
        }
    }
}

struct TabViewModifier: ViewModifier {
    var color: Color
    func body(content: Content) -> some View {
        if #available(iOS 16, *) {
            content.toolbarBackground(color, for: .tabBar)
        } else {
            content.onAppear {
                UITabBar.appearance().barTintColor = UIColor(color)
            }
        }
    }
}

extension View {
    @ViewBuilder
    func accent(color: Color) -> some View {
        self.modifier(AccentModifier(color: color))
    }
    
    @ViewBuilder
    func tabBarColor(color: Color) -> some View {
        self.modifier(TabViewModifier(color: color))
    }
}
