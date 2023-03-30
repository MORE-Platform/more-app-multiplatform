//
//  MoreFilterList.swift
//  iosApp
//
//  Created by Isabella Aigner on 28.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI

struct MoreFilterOptionList: View {
    var multiSelect = false
    @Binding var title: String
    @Binding var selectOptionList: Set<String>
    
    @State var selectedValueList: Set<String> = []
    @State var selectedValue: String = ""
    
    let optionCallback: (String, Bool) -> ()
    
    var body: some View {
        
        VStack(alignment: .leading) {
            SectionHeading(sectionTitle: .constant(title))
                .padding(15)
            Divider()
            
            ForEach(selectOptionList.sorted(), id: \.self) { filter in

                    MoreFilterOption(label: filter, selected: multiSelect ? isSelectedMultiValue(label: filter) : isSelectedSingleValue(label: filter), callback: {_ in
                        
                        if multiSelect {
                            if selectedValueList.contains(filter) {
                                self.selectedValueList.remove(filter)
                                optionCallback(filter, false)
                            } else {
                                self.selectedValueList.insert(filter)
                                optionCallback(filter, true)
                            }
                           
                        } else {
                            if self.selectedValue == filter {
                                self.selectedValue = "Today and Tomorrow"
                                optionCallback(self.selectedValue, false)
                            } else {
                                self.selectedValue = filter
                                optionCallback(self.selectedValue, true)
                            }
                        }
                    })
                
                Divider()
            }
        }
    }

    func isSelectedMultiValue(label: String) -> Bool {
        if self.selectedValueList.contains(label) {
            return true
        }
        return false
    }
    func isSelectedSingleValue(label: String) -> Bool {
        print(label)
        print(self.selectedValue)
        if label == self.selectedValue {
                return true
        }
        return false
    }
}
