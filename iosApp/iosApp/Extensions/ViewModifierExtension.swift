//
//  ViewModifierExtension.swift
//  iosApp
//
//  Created by Jan Cortiel on 15.03.23.
//  Copyright © 2023 orgName. All rights reserved.
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

extension View {
    @ViewBuilder
    func accent(color: Color) -> some View {
        self.modifier(AccentModifier(color: color))
    }
}
