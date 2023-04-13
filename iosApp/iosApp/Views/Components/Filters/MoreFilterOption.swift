//
//  MoreFilterOption.swift
//  iosApp
//
//  Created by Isabella Aigner on 28.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI

struct MoreFilterOption: View {
    @Binding var selectedValuesInList: [String]
    @State private var isSelected: Bool = false
    var option: String
    var label: String
    var callback: () -> Void

    init(selectedValuesInList: Binding<[String]>, option: String, label: String, callback: @escaping () -> Void) {
        self._selectedValuesInList = selectedValuesInList
        self.option = option
        self.label = label
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
                    MoreFilterText(text: .constant(label), isSelected: $isSelected)
                }
            }
            .padding(5)
        }
        .onAppear {
            if option == "All Items" && selectedValuesInList.isEmpty {
                isSelected = true
            } else {
                isSelected = selectedValuesInList.contains(option)
            }
        }
        .onChange(of: selectedValuesInList, perform: { _ in
            if option == "All Items" && selectedValuesInList.isEmpty {
                isSelected = true
            } else {
                isSelected = selectedValuesInList.contains(option)
            }
        })
    }
}
