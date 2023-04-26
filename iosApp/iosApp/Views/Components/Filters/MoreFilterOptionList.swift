//
//  MoreFilterList.swift
//  iosApp
//
//  Created by Isabella Aigner on 28.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI

struct MoreFilterOptionList: View {
    
    @Binding var title: String
    @Binding var optionList: [String]
    @State var selectedValueList: [String]
    let multiSelect: Bool
    let updateFilters: (Bool, String, [String], String) -> ([String])
    let isItemSelected: ([String], String) -> (Bool)
    let stringTable: String
    
    var body: some View {
        
        VStack(alignment: .leading) {
            SectionHeading(sectionTitle: .constant(title))
                .padding(15)
            Divider()
            
            ForEach(optionList, id: \.self) { filter in
                Button {
                    selectedValueList = updateFilters(multiSelect, filter, selectedValueList, stringTable)
                } label: {
                    HStack {
                        MoreFilterOption(
                            multiSelect: multiSelect,
                            option: filter,
                            selectedValuesInList: $selectedValueList,
                            isItemSelected: isItemSelected,
                            stringTable: stringTable)
                        Spacer()
                    }
                }
                .buttonStyle(.borderless)
                .frame(maxWidth: .infinity)
                Divider()
            }
        }
    }
}
