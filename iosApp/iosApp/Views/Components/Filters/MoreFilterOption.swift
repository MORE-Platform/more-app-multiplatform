//
//  MoreFilterOption.swift
//  iosApp
//
//  Created by Isabella Aigner on 28.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI

struct MoreFilterOption: View {
    
    var option: String
    @Binding var selectedValuesInList: [String]
    let multiSelect: Bool
    @EnvironmentObject var dashboardFilterViewModel: DashboardFilterViewModel
    
    @State private var isSelected: Bool = false
    private let stringTable = "DashboardFilter"

    init(multiSelect: Bool, selectedValuesInList: Binding<[String]>, option: String) {
        self._selectedValuesInList = selectedValuesInList
        self.option = option
        self.multiSelect = multiSelect
        self.isSelected = self.selectedValuesInList.contains(self.option)
    }
    
    var body: some View {
        VStack {
            HStack {
                if isSelected {
                    Image(systemName: "checkmark")
                        .foregroundColor(.more.approved)
                } else {
                    Spacer()
                        .frame(width: 5)
                }
                Button(action: {
                    selectedValuesInList = (dashboardFilterViewModel.updateFilters(multiSelect: multiSelect, filter: option, list: selectedValuesInList, stringTable: stringTable))
                }) {
                    MoreFilterText(text: .constant(String.localizedString(forKey: option, inTable: stringTable, withComment: "String representation of observation type")), isSelected: $isSelected)
                }
            }
            .padding(5)
        }
        .onAppear {
            isSelected = dashboardFilterViewModel.isItemSelected(selectedValuesInList: selectedValuesInList, option: option)
        }
        .onChange(of: selectedValuesInList, perform: { _ in
            isSelected = dashboardFilterViewModel.isItemSelected(selectedValuesInList: selectedValuesInList, option: option)
        })
    }
}
