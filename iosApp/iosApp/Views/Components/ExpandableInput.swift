//
//  ExpandableInput.swift
//  iosApp
//
//  Created by Isabella Aigner on 27.03.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import SwiftUI

struct ExpandableInput: View {
    @Binding var expanded: Bool

    @Binding var isSmTextfield: Bool
    @Binding var headerText: String
    @Binding var inputPlaceholder: String

    @Binding var input: String

    var uppercase: Bool = false
    var autoCorrectDisabled: Bool = false
    var textType: UITextContentType? = nil

    var body: some View {
        VStack(alignment: .center) {
            Button {
                self.expanded.toggle()
            } label: {
                HStack {
                    SectionHeading(sectionTitle: headerText)
                    UIToggleFoldViewButton(isOpen: $expanded)
                }
                .padding()
            }
            .buttonStyle(.plain)
            .frame(height: 40)

            if expanded {
                if isSmTextfield {
                    MoreTextFieldSmBottom(titleKey: .constant(inputPlaceholder), inputText: $input, uppercase: uppercase, textType: textType)
                } else {
                    MoreTextField(titleKey: .constant(inputPlaceholder), inputText: $input, uppercase: uppercase, textType: textType)
                }
            }
        }
        .animation(.more.foldingAnimation, value: expanded)
    }
}

struct ExpandableInput_Preview: PreviewProvider {
    static var previews: some View {
        ExpandableInput(expanded: .constant(true), isSmTextfield: .constant(false), headerText: .constant("Text for header"), inputPlaceholder: .constant("Placeholdertext for input"), input: .constant("me"), uppercase: false)
    }
}
