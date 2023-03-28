//
//  MoreActionButton.swift
//  iosApp
//
//  Created by Jan Cortiel on 06.02.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI

struct MoreActionButton<ButtonLabel: View>: View {
    var color: Color = .more.primary
    var backgroundColor = Color.more.primary
    var disabeldColor = Color.more.secondaryMedium
    var disabledBackgroundColor = Color.more.primaryLight200
    var disabeldBorderColor = Color.more.secondaryMedium
    @Binding var disabled: Bool
    var alertOpen: Binding<Bool> = .constant(false)
    let action: () -> Void
    var label: () -> ButtonLabel
    var errorAlert: () -> Alert = {Alert(title: Text("Alert"), dismissButton: .default(Text("Ok")))}

    var body: some View {
        Button(action: action, label: label)
            .disabled(disabled)
            .frame(maxWidth: .infinity)
            .padding()
            .foregroundColor(disabled ? disabeldColor : .more.white)
            .background(disabled ? disabledBackgroundColor : backgroundColor)
            .cornerRadius(.moreBorder.cornerRadius)
            .alert(isPresented: alertOpen, content: errorAlert)
            .overlay(
                RoundedRectangle(cornerRadius: .moreBorder.cornerRadius)
                    .stroke(disabled ? disabeldBorderColor : backgroundColor, lineWidth: 1)
            )
    }
}

extension MoreActionButton {
}

struct MoreActionButton_Previews: PreviewProvider {
    @State var open = false
    static var previews: some View {
        MoreActionButton(disabled: .constant(false), action: {}) {
            Text("Action Button")
        }
    }
}
