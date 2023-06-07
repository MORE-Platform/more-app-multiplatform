//
//  MoreTextField.swift
//  iosApp
//
//  Created by Jan Cortiel on 06.02.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import SwiftUI

struct MoreTextField: View {
    @Binding var titleKey: String
    @Binding var inputText: String
    let uppercase: Bool
    var body: some View {
        TextField(titleKey, text: $inputText)
            .textFieldAutoCapitalizataion(uppercase: uppercase)
            .padding(.moreTextFieldPadding.textFieldInnerPadding)
            .overlay(
                RoundedRectangle(cornerRadius: .moreBorder.cornerRadius)
                    .stroke(Color.more.primaryMedium, lineWidth: .moreBorder.lineWidth)
            )
            .background(Color.more.primaryLight)
            
    }
}

struct MoreTextField_Previews: PreviewProvider {
    
    static var previews: some View {
        MoreTextField(titleKey: .constant("Hello World Key"), inputText: .constant(""), uppercase: true)
    }
}
