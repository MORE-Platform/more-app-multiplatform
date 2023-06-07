//
//  MoreTextFieldSmBottom.swift
//  iosApp
//
//  Created by Isabella Aigner on 23.03.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import Foundation

import SwiftUI

struct MoreTextFieldSmBottom: View {
    @Binding var titleKey: String
    @Binding var inputText: String
    var uppercase: Bool = false
    var autoCorrectDisabled: Bool = false
    var textType: UITextContentType? = nil
    var body: some View {
        TextField(titleKey, text: $inputText)
            .textFieldAutoCapitalizataion(uppercase: uppercase)
            .autocorrectionDisabled(autoCorrectDisabled)
            .textContentType(textType)
            .padding(.moreTextFieldPadding.textFieldInnerPadding)
            .background(Color.more.primaryLight)
            .font(.system(size: 14))
            .foregroundColor(Color.more.secondaryMedium)
            .overlay(
                Rectangle()
                    .frame(height: 1.0)
                    .foregroundColor(Color.more.primaryMedium)
                    .offset(x: 0, y: 20)
            )
        
            
    }
}

struct MoreTextFieldSmBottom_Previews: PreviewProvider {
    
    static var previews: some View {
        MoreTextFieldSmBottom(titleKey: .constant("Hello World Key"), inputText: .constant(""), uppercase: false)
    }
}
