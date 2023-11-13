//
//  ViewModifierExtension.swift
//  iosApp
//
//  Created by Jan Cortiel on 15.03.23.
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
