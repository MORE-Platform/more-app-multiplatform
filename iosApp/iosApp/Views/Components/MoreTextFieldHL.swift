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
//  Licensed under the Apache 2.0 license with Commons Clause 
//  (see https://www.apache.org/licenses/LICENSE-2.0 and
//  https://commonsclause.com/).
//
import SwiftUI

struct MoreTextFieldHL: View {
    @Binding var isSmTextfield: Bool
    var headerText: String
    @Binding var inputPlaceholder: String
    
    @Binding var input: String
    
    var uppercase: Bool = false
    var autoCorrectDisabled = false
    var textType: UITextContentType? = nil
    var body: some View {
        VStack(alignment: .leading) {
            
            HStack{
                Spacer()
                SectionHeading(sectionTitle: headerText)
                Spacer()
            }
            .padding(3)
            
            if isSmTextfield {
                MoreTextFieldSmBottom(titleKey: .constant(inputPlaceholder),inputText: $input, uppercase: uppercase, autoCorrectDisabled: autoCorrectDisabled, textType: textType)
            } else {
                MoreTextField(titleKey: .constant(inputPlaceholder), inputText: $input, uppercase: uppercase, autoCorrectDisabled: autoCorrectDisabled, textType: textType)
            }
            
        }
            
    }
}

struct MoreTextFieldHL_Previews: PreviewProvider {
    
    static var previews: some View {
        MoreTextFieldHL(isSmTextfield: .constant(false), headerText: "Hello World Key", inputPlaceholder: .constant(""), input: .constant("me"), uppercase: false)
    }
}
