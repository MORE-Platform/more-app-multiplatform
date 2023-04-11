//
//  MoreFilterList.swift
//  iosApp
//
//  Created by Isabella Aigner on 28.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI

struct MoreFilterMultiOptionList: View {
    @Binding var stringTable: String
    @Binding var title: String
    @Binding var optionList: [String]
    @State var selectedValueList: [String] = []
    let optionCallback: ([String]) -> ()
    
    var body: some View {
        
        VStack(alignment: .leading) {
            /* SectionHeading(sectionTitle: .constant(title))
                .padding(15)
            Divider()
            
            ForEach(optionList, id: \.self) { filter in

                    MoreFilterOption(
                        label: String.localizedString(forKey: filter, inTable: stringTable, withComment: "Multi selection filter option"),
                        selected: .constant(false),
                        callback: {
                            if filter != "All Items" {
                                if selectedValueList.contains("All Items") {
                                    selectedValueList.remove(at: selectedValueList.firstIndex(of: "All Items")!)
                                }
                                if selectedValueList.contains(filter) {
                                    selectedValueList.remove(at: selectedValueList.firstIndex(of: filter)!)
                                } else {
                                    selectedValueList.append(filter)
                                }
                            } else {
                                selectedValueList = ["All Items"]
                            }
                    })
                
                Divider()
            } */
        }.onChange(of: selectedValueList, perform: { value in
            print(value)
            optionCallback(selectedValueList)
        })
    }
    
    func isSelectedMultiValue(value: String, label: String) -> Bool {
        if self.selectedValueList.contains(value) {
            return true
        }
        return false
    }

}
