//
//  UIToggleButton.swift
//  iosApp
//
//  Created by Jan Cortiel on 06.02.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import SwiftUI

struct UIToggleFoldViewButton: View {
    @Binding var isOpen: Bool

    var body: some View {
        Image(systemName: "chevron.up")
            .rotationEffect(Angle(degrees: isOpen ? 0 : 180))
            .animation(.more.foldingAnimation, value: isOpen)
    }
}

struct UIToggleFoldViewButton_Previews: PreviewProvider {
    static var previews: some View {
        UIToggleFoldViewButton(isOpen: .constant(true))
    }
}
