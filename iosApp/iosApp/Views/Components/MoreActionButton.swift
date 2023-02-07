//
//  MoreActionButton.swift
//  iosApp
//
//  Created by Jan Cortiel on 06.02.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI

struct MoreActionButton<ButtonLabel: View>: View {
    let action: () -> Void
    var label: () -> ButtonLabel
    var body: some View {
        Button(action: action, label: label)
            .frame(minWidth: .moreFrameStyle.buttonMaxWidth, maxWidth: .moreFrameStyle.buttonMaxWidth)
            .padding()
            .foregroundColor(.more.white)
            .background(Color.more.main)
            .cornerRadius(.moreBorder.cornerRadius)
    }
}

struct MoreActionButton_Previews: PreviewProvider {
    static var previews: some View {
        MoreActionButton(action: {}) {
            Text("Hello World")
        }
    }
}
