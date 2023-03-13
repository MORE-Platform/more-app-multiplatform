//
//  MoreActionButton.swift
//  iosApp
//
//  Created by Jan Cortiel on 06.02.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI

struct MoreActionButton<ButtonLabel: View>: View {
    var backgroundColor: Color = .more.main
    var alertOpen: Binding<Bool> = .constant(false)
    let action: () -> Void
    var label: () -> ButtonLabel
    var errorAlert: () -> Alert = {Alert(title: Text("Alert"), dismissButton: .default(Text("Ok")))}

    var body: some View {
        Button(action: action, label: label)
            .frame(maxWidth: .infinity)
            .padding()
            .foregroundColor(.more.white)
            .background(backgroundColor)
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
