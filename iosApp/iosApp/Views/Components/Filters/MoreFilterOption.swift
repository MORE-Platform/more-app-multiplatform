//
//  MoreFilterOption.swift
//  iosApp
//
//  Created by Isabella Aigner on 28.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI

struct MoreFilterOption: View {
    
    let multiSelect: Bool
    var option: String
    @Binding var selectedValuesInList: [String]
    let isItemSelected: ([String], String) -> (Bool)
    
    @State private var isSelected: Bool = false
    let stringTable: String
    
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
                MoreFilterText(text: .constant(String.localizedString(forKey: option, inTable: stringTable, withComment: "String representation of \(option) option")))
            }
            .padding(5)
        }
        .onAppear {
            isSelected = isItemSelected(selectedValuesInList, option)
        }
        .onChange(of: selectedValuesInList, perform: { _ in
            isSelected = isItemSelected(selectedValuesInList, option)
        })
    }
}
