//
//  MoreActionButton.swift
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

struct MoreActionButton<ButtonLabel: View>: View {
    var backgroundColor = Color.more.primary
    var maxWidth: CGFloat = .infinity
    @Binding var disabled: Bool
    var disabeldColor = Color.more.secondaryMedium
    var disabledBackgroundColor = Color.more.primaryLight200
    var disabeldBorderColor = Color.more.secondaryMedium
    var alertOpen: Binding<Bool> = .constant(false)
    let action: () -> Void
    var label: () -> ButtonLabel
    var errorAlert: () -> Alert = {Alert(title: Text("Alert"), dismissButton: .default(Text("Ok")))}

    var body: some View {
        Button(action: action){
            label()
        .frame(maxWidth: maxWidth)
        .padding()
        .foregroundColor(disabled ? disabeldColor : .more.white)
        .background(disabled ? disabledBackgroundColor : backgroundColor)
        .cornerRadius(.moreBorder.cornerRadius)
        .overlay(
            RoundedRectangle(cornerRadius: .moreBorder.cornerRadius)
                .stroke(disabled ? disabeldBorderColor : backgroundColor, lineWidth: 1)
        )
    }
        .disabled(disabled)
        .alert(isPresented: alertOpen, content: errorAlert)
    }
}

struct MoreActionButton_Previews: PreviewProvider {
    @State var open = false
    static var previews: some View {
        MoreActionButton(disabled: .constant(false), action: {}) {
            Text("Action Button")
        }
    }
}
