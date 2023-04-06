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
    @Binding var optionList: [String]
    private let dashboardStringTable: String = "DashboardFilter"
    
    @State var selectedValueList: [String] = []
    @State var selectedValue: String = ""
    @State var selectedValueIndex: Int? = 1
    
    let optionCallback: (String, Bool) -> ()
    
    
    var body: some View {
        
        VStack(alignment: .leading) {
            SectionHeading(sectionTitle: .constant(title))
                .padding(15)
            Divider()
            
            ForEach(optionList, id: \.self) { filter in

                    MoreFilterOption(
                        label: String.localizedString(forKey: filter, inTable: dashboardStringTable, withComment: "Timeframe filter option"),
                        selected: multiSelect ? isSelectedMultiValue(label: filter) : isSelectedSingleValue(label: filter),
                        callback: {_ in
                        
                        if multiSelect {
                            if selectedValueList.contains(filter) {
                                if ((selectedValueList.firstIndex(of: filter)) != nil) {
                                    let index = selectedValueList.firstIndex(of: filter)
                                    self.selectedValueList.remove(at: index ?? -1)
                                    optionCallback(filter, false)
                                }
                                
                            } else {
                                self.selectedValueList.append(filter)
                                
                                if(filter == "All Filter") {
                                    optionCallback("reset", false)
                                } else {
                                    optionCallback(filter, true)
                                }
                            }
                           
                        } else {
                            if self.selectedValue == filter {
                                self.selectedValue = "Today and Tomorrow"
                                optionCallback("TODAY_AND_TOMORROW", false)
                            } else {
                                self.selectedValue = String(describing: filter)
                                self.selectedValueIndex = optionList.firstIndex(of: filter)
                                optionCallback(filter, true)
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
        //print(self.selectedValue)
        if label == String(describing: self.selectedValue) {
                return true
        }
        return false
    }
     
     
}
