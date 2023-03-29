//
//  ExpandableInput.swift
//  iosApp
//
//  Created by Isabella Aigner on 27.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI

struct ExpandableInput: View {
    @Binding var expanded: Bool
    
    @Binding var isSmTextfield: Bool
    @Binding var headerText: String
    @Binding var inputPlaceholder: String
    
    @Binding var input: String
    
    var body: some View {
        VStack(alignment: .leading) {
            
            HStack{
                Spacer()
                SectionHeading(sectionTitle: .constant(headerText))
                UIToggleFoldViewButton(isOpen: $expanded)
                Spacer()
            }
            .padding(3)
            
            if expanded {
                if isSmTextfield {
                    MoreTextFieldSmBottom(titleKey: .constant(inputPlaceholder),inputText: $input)
                } else {
                    MoreTextField(titleKey: .constant(inputPlaceholder), inputText: $input)
                }
            }
            
        }
    }
}

struct ExpandableInput_Preview: PreviewProvider {
    static var previews: some View {
        ExpandableInput(expanded: .constant(true), isSmTextfield: .constant(false), headerText: .constant("Text for header"), inputPlaceholder: .constant("Placeholdertext for input"), input: .constant("me"))
    }
}
