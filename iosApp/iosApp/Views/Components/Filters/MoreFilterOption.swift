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
    @EnvironmentObject var dashboardFilterViewModel: DashboardFilterViewModel
    
    @State private var isSelected: Bool = false
    private let stringTable = "DashboardFilter"
    
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
                MoreFilterText(text: .constant(String.localizedString(forKey: option, inTable: stringTable, withComment: "String representation of observation type")), isSelected: $isSelected)
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
