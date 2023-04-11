//
//  MoreFilterList.swift
//  iosApp
//
//  Created by Isabella Aigner on 28.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI

struct MoreFiltreSingleOptionList: View {
    @Binding var stringTable: String
    @Binding var title: String
    @Binding var optionList: [String]
    @State var selectedValue: String = ""
    let optionCallback: (String) -> ()
    
    var body: some View {
        
        VStack(alignment: .leading) {
            /* SectionHeading(sectionTitle: .constant(title))
                .padding(15)
            Divider()
            
            ForEach(optionList, id: \.self) { filter in
                MoreFilterOption(
                    label: String.localizedString(forKey: filter, inTable: stringTable, withComment: "Single filter option"),
                    selected: .constant(false),
                    callback: {
                        if self.selectedValue == filter {
                            self.selectedValue = "ENTIRE_TIME"
                        } else {
                            self.selectedValue = String(describing: filter)
                        }
                    })
                Divider()
            } */
        }.onChange(of: selectedValue, perform: { value in
            optionCallback(value)
        })
    }
    
    func isSelectedSingleValue(label: String) -> Bool {
        if label == String(describing: self.selectedValue) {
            return true
        }
        return false
    }
    
    
}
