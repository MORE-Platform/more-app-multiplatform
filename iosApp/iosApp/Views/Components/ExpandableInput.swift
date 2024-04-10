//
//  ExpandableInput.swift
//  iosApp
//
//  Created by Isabella Aigner on 27.03.23.
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

struct ExpandableInput: View {
    @Binding var expanded: Bool

    @Binding var isSmTextfield: Bool
    @Binding var headerText: String
    @Binding var inputPlaceholder: String

    @Binding var input: String

    var capitalization: Capitalization = .normal
    var autoCorrectDisabled: Bool = false
    var textType: UITextContentType? = nil

    var body: some View {
        VStack(alignment: .center) {
            Button {
                self.expanded.toggle()
            } label: {
                HStack {
                    SectionHeading(sectionTitle: headerText)
                    Image(systemName: "chevron.up")
                        .rotationEffect(Angle(degrees: expanded ? 0 : 180))
                        .animation(.more.foldingAnimation, value: expanded)
                }
                .padding()
            }
            .buttonStyle(.plain)
            .contentShape(Rectangle())
            .frame(height: 45)

            if expanded {
                if isSmTextfield {
                    MoreTextFieldSmBottom(titleKey: .constant(inputPlaceholder), inputText: $input, capitalization: capitalization, autoCorrectDisabled: true, textType: textType)
                } else {
                    MoreTextField(titleKey: .constant(inputPlaceholder), inputText: $input, capitalization: capitalization, autoCorrectDisabled: true, textType: textType)
                }
            }
        }
        .animation(.more.foldingAnimation, value: expanded)
    }
}

struct ExpandableInput_Preview: PreviewProvider {
    static var previews: some View {
        ExpandableInput(expanded: .constant(true), isSmTextfield: .constant(false), headerText: .constant("Text for header"), inputPlaceholder: .constant("Placeholdertext for input"), input: .constant("me"), capitalization: .normal)
    }
}
