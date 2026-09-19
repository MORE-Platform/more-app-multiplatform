//
//  MoreTextFieldHL.swift
//  iosApp
//
//  Created by Isabella Aigner on 28.03.23.
//  Copyright © 2023 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung
//  Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
//
import SwiftUI

struct MoreTextFieldHL: View {
    @Binding var isSmTextfield: Bool
    var headerText: String
    @Binding var inputPlaceholder: String

    @Binding var input: String

    var capitalization: Capitalization = .normal
    var autoCorrectDisabled = false
    var textType: UITextContentType? = nil
    var hlAlignment: TextAlignment = .leading

    var body: some View {
        VStack(alignment: .leading) {
            HStack {
                Spacer()
                SectionHeading(sectionTitle: headerText, showAllText: true)
                    .multilineTextAlignment(hlAlignment)
                Spacer()
            }
            .padding(3)

            if isSmTextfield {
                MoreTextFieldSmBottom(titleKey: $inputPlaceholder, inputText: $input, capitalization: capitalization, autoCorrectDisabled: autoCorrectDisabled, textType: textType)
            } else {
                MoreTextField(titleKey: $inputPlaceholder, inputText: $input, capitalization: capitalization, autoCorrectDisabled: autoCorrectDisabled, textType: textType)
            }
        }
    }
}

struct MoreTextFieldHL_Previews: PreviewProvider {
    static var previews: some View {
        MoreTextFieldHL(isSmTextfield: .constant(false), headerText: "Hello World Key", inputPlaceholder: .constant(""), input: .constant("me"), capitalization: .normal)
    }
}
