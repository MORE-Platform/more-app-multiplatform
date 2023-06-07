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
    let uppercase: Bool
    var body: some View {
        TextField(titleKey, text: $inputText)
            .textFieldAutoCapitalizataion(uppercase: uppercase)
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
