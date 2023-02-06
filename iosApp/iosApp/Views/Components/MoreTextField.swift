//
//  MoreTextField.swift
//  iosApp
//
//  Created by Jan Cortiel on 06.02.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI

struct MoreTextField: View {
    @Binding var titleKey: String
    @Binding var inputText: String
    var body: some View {
        TextField(titleKey, text: $inputText)
            .padding(.morePadding.textField)
            .overlay(
                RoundedRectangle(cornerRadius: .moreCornerRadius.textField)
                    .stroke(Color.more.main, lineWidth: .moreLineWidth.textField)
            )
            .padding(.moreEdgeInsets.textFieldBottom)
    }
}

struct MoreTextField_Previews: PreviewProvider {
    
    static var previews: some View {
        MoreTextField(titleKey: .constant("Hello World Key"), inputText: .constant(""))
    }
}
