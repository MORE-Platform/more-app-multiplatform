//
//  UIToggleButton.swift
//  iosApp
//
//  Created by Jan Cortiel on 06.02.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI

struct UIToggleFoldViewButton: View {
    @Binding var isOpen: Bool
    @State private var rotationAngle = 0.0
    var body: some View {
        Button {
            withAnimation(.more.foldingAnimation) {
                if !isOpen {
                    rotationAngle += 180
                } else {
                    rotationAngle -= 180
                }
            }
            isOpen.toggle()
        } label: {
            Image.more.toggleFoldView
                .rotationEffect(Angle(degrees: rotationAngle))
        }
    }
}

struct UIToggleFoldViewButton_Previews: PreviewProvider {
    static var previews: some View {
        UIToggleFoldViewButton(isOpen: .constant(true))
    }
}
