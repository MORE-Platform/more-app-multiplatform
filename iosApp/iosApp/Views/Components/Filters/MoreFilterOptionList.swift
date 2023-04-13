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
    
    let optionCallback: ([String]) -> ()
    
    
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
                            if filter == "All Items" {
                                selectedValueList.removeAll()
                            } else {
                                if selectedValueList.contains(filter) {
                                    self.selectedValueList.remove(at: selectedValueList.firstIndex(of: filter)!)
                                } else {
                                    self.selectedValueList.append(filter)
                                }
                            }
                        } else {
                            if !selectedValueList.isEmpty {
                                selectedValueList.removeAll()
                            }
                            selectedValueList.append(filter)
                        }
                        optionCallback(selectedValueList)
                    })
                Divider()
            }
        }
    }
}
