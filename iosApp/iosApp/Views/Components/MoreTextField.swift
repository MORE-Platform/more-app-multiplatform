//
//  MoreTextField.swift
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

struct MoreTextField: View {
    @Binding var titleKey: String
    @Binding var inputText: String
    var capitalization: Capitalization = .normal
    var autoCorrectDisabled: Bool = false
    var textType: UITextContentType? = nil

    var body: some View {
        TextField(titleKey, text: $inputText)
            .textFieldAutoCapitalizataion(capitalization: capitalization)
            .autocorrectionDisabled(autoCorrectDisabled)
            .textContentType(textType)
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
        MoreTextField(titleKey: .constant("Hello World Key"), inputText: .constant(""), capitalization: .normal)
    }
}
