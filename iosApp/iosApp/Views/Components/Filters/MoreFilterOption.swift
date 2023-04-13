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
    var callback: () -> Void
    @Binding var selectedValuesInList: [String]
    
    @State private var isSelected: Bool = false
    private let stringTable = "DashboardFilter"

    init(selectedValuesInList: Binding<[String]>, option: String, callback: @escaping () -> Void) {
        self._selectedValuesInList = selectedValuesInList
        self.option = option
        self.callback = callback
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
                    callback()
                }) {
                    MoreFilterText(text: .constant(String.localizedString(forKey: option, inTable: stringTable, withComment: "String representation of observation type")), isSelected: $isSelected)
                }
            }
            .padding(5)
        }
        .onAppear {
            let allItemsString = String.localizedString(forKey: "All Items", inTable: stringTable, withComment: "String for All Items")
            if option == allItemsString && selectedValuesInList.isEmpty {
                isSelected = true
            } else {
                isSelected = selectedValuesInList.contains(option)
            }
        }
        .onChange(of: selectedValuesInList, perform: { _ in
            let allItemsString = String.localizedString(forKey: "All Items", inTable: stringTable, withComment: "String for All Items")
            if option == allItemsString && selectedValuesInList.isEmpty {
                isSelected = true
            } else {
                isSelected = selectedValuesInList.contains(option)
            }
        })
    }
}
