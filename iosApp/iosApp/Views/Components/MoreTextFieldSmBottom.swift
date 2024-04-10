//
//  MoreTextFieldSmBottom.swift
//  iosApp
//
//  Created by Isabella Aigner on 23.03.23.
//  Copyright © 2023 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung 
//  Licensed under the Apache 2.0 license with Commons Clause 
//  (see https://www.apache.org/licenses/LICENSE-2.0 and
//  https://commonsclause.com/).
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
