//
//  MoreFilterList.swift
//  iosApp
//
//  Created by Isabella Aigner on 28.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI

struct MoreFilterOptionList: View {
    
    @EnvironmentObject var dashboardFilterViewModel: DashboardFilterViewModel
    
    @Binding var title: String
    @Binding var optionList: [String]
    @State var selectedValueList: [String]
    let multiSelect: Bool
    
    private let stringTable: String = "DashboardFilter"
    
    var body: some View {
        
        VStack(alignment: .leading) {
            SectionHeading(sectionTitle: .constant(title))
                .padding(15)
            Divider()
            
            ForEach(optionList, id: \.self) { filter in
                Button {
                    selectedValueList = dashboardFilterViewModel.updateFilters(multiSelect: multiSelect, filter: filter, list: selectedValueList, stringTable: stringTable)
                } label: {
                    HStack {
                        MoreFilterOption(
                            multiSelect: multiSelect, option: filter, selectedValuesInList: $selectedValueList)
                        .environmentObject(dashboardFilterViewModel)
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
