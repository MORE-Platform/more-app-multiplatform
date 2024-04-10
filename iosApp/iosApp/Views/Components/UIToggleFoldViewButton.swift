//
//  UIToggleButton.swift
//  iosApp
//
//  Created by Jan Cortiel on 06.02.23.
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
