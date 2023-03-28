//
//  MoreTextFieldHL.swift
//  iosApp
//
//  Created by Isabella Aigner on 28.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//
import SwiftUI

struct MoreTextFieldHL: View {
    @Binding var isSmTextfield: Bool
    @Binding var headerText: String
    @Binding var inputPlaceholder: String
    
    @Binding var input: String
    
    var body: some View {
        VStack(alignment: .leading) {
            
            HStack{
                Spacer()
                SectionHeading(sectionTitle: .constant(headerText))
                Spacer()
            }
            .padding(3)
            
            if isSmTextfield {
                MoreTextFieldSmBottom(titleKey: .constant(inputPlaceholder),inputText: $input)
            } else {
                MoreTextField(titleKey: .constant(inputPlaceholder), inputText: $input)
            }
            
        }
            
    }
}

struct MoreTextFieldHL_Previews: PreviewProvider {
    
    static var previews: some View {
        MoreTextFieldHL(isSmTextfield: .constant(false), headerText: .constant("Hello World Key"), inputPlaceholder: .constant(""), input: .constant("me"))
    }
}
