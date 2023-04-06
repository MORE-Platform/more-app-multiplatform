//
//  MoreFilterList.swift
//  iosApp
//
//  Created by Isabella Aigner on 28.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI

struct MoreFilterMultiOptionList: View {
    private let observationStringTable: String = "ObservationTypes"
    @Binding var title: String
    @Binding var optionList: [String]
    @State var selectedValueList: [String] = []
    let optionCallback: (String, Bool) -> ()
    
    var body: some View {
        
        VStack(alignment: .leading) {
            SectionHeading(sectionTitle: .constant(title))
                .padding(15)
            Divider()
            
            ForEach(optionList, id: \.self) { filter in

                    MoreFilterOption(
                        label: String.localizedString(forKey: filter, inTable: observationStringTable, withComment: "Observation filter option"),
                        selected: isSelectedMultiValue(
                            value: filter,
                            label: String.localizedString(forKey: filter, inTable: observationStringTable, withComment: "Observation filter option")
                        ),
                        callback: {_ in
                            if filter != "All Items" && self.selectedValueList.contains("All Items") {
                                let allIndex = selectedValueList.firstIndex(of: "All Items")
                                if (allIndex != nil) {
                                    self.selectedValueList.remove(at: allIndex ?? -1)
                                }
                            }
                            if selectedValueList.contains(filter) && filter != "All Items" {
                                if (selectedValueList.firstIndex(of: filter) != nil) {
                                    let index = selectedValueList.firstIndex(of: filter)
                                    if index != nil {
                                        self.selectedValueList.remove(at: index ?? -1)
                                    }
                                    optionCallback(filter, false)
                                }
                            } else {
                                if(filter == "" || filter == "All Items") {
                                    self.selectedValueList = ["All Items"]
                                    optionCallback("reset", false)
                                } else {
                                    self.selectedValueList.append(filter)
                                    optionCallback(filter, true)
                                }
                            }
                    })
                
                Divider()
            }
        }
    }
    
    func isSelectedMultiValue(value: String, label: String) -> Bool {
        if self.selectedValueList.contains(value) {
            return true
        }
        return false
    }

}
