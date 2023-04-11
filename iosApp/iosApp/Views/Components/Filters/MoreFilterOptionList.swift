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
    
    @State var selectedValueList: [String]
    
    let optionCallback: (String, Bool) -> ()
    
    
    var body: some View {
        
        VStack(alignment: .leading) {
            SectionHeading(sectionTitle: .constant(title))
                .padding(15)
            Divider()
            
            ForEach(optionList, id: \.self) { filter in
                
                MoreFilterOption(
                    selectedValuesInList: $selectedValueList, option: filter, label: String.localizedString(forKey: filter, inTable: dashboardStringTable, withComment: "Timeframe filter option"),
                    callback: {
                        if multiSelect {
                            print(filter)
                            if filter == "All Filter" {
                                selectedValueList.forEach { value in
                                    selectedValueList = selectedValueList.filter { $0 == "All Filter" }
                                }
                            } else {
                                if selectedValueList.contains(filter) {
                                    self.selectedValueList.remove(at: selectedValueList.firstIndex(of: filter)!)
                                } else {
                                    self.selectedValueList.append(filter)
                                }
                            }
                        } else {
                            if selectedValueList.contains(filter) {
                                selectedValueList.removeAll()
                                selectedValueList.append("ENTIRE_TIME")
                            } else {
                                if !selectedValueList.isEmpty {
                                    selectedValueList.removeAll()
                                }
                                selectedValueList.append(filter)
                            }
                            print("single filter: \(selectedValueList)")
                        }
                    })
                
                Divider()
            }
        }
    }
}
