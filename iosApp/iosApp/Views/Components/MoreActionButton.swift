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
    var alertOpen: Binding<Bool> = .constant(false)
    let action: () -> Void
    var label: () -> ButtonLabel
    var errorAlert: () -> Alert = {Alert(title: Text("Alert"), dismissButton: .default(Text("Ok")))}

    var body: some View {
        Button(action: action, label: label)
            .frame(minWidth: .moreFrameStyle.buttonMaxWidth, maxWidth: .moreFrameStyle.buttonMaxWidth)
            .padding()
            .foregroundColor(.more.white)
            .background(color)
            .cornerRadius(.moreBorder.cornerRadius)
            .alert(isPresented: alertOpen, content: errorAlert)
    }
}

extension MoreActionButton {
}

struct MoreActionButton_Previews: PreviewProvider {
    @State var open = false
    static var previews: some View {
        MoreActionButton(action: {}) {
            Text("Action Button")
        }
    }
}
