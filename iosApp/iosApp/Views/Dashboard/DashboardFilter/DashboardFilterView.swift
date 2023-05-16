//
//  DashboardFilterView.swift
//  iosApp
//
//  Created by Isabella Aigner on 28.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI
import shared

struct DashboardFilterView: View {
    @EnvironmentObject var viewModel: DashboardFilterViewModel
    let stringTable = "DashboardFilter"
    let navigationStrings = "Navigation"
    @State var filtersChanged = false
    
    var body: some View {
        Navigation {
            MoreMainBackgroundView {
                VStack(alignment: .leading) {
                    VStack {
                        VStack {
                            MoreFilterOptionList(
                                title: .constant(String.localizedString(forKey: "Select Time", inTable: stringTable, withComment: "Set time filter")),
                                optionList: .constant(viewModel.dateFilterStringList),
                                selectedValueList: [viewModel.dateFilter.name],
                                multiSelect: false,
                                updateFilters: viewModel.updateFilters,
                                isItemSelected: viewModel.isItemSelected,
                                stringTable: stringTable)
                        }.padding(.vertical, 20)
                        VStack {
                            MoreFilterOptionList(
                                title: .constant(String.localizedString(forKey: "Select Type", inTable: stringTable, withComment: "Set type filter")),
                                optionList: .constant(viewModel.observationTypes),
                                selectedValueList: viewModel.observationTypeFilter,
                                multiSelect: true,
                                updateFilters: viewModel.updateFilters,
                                isItemSelected: viewModel.isItemSelected,
                                stringTable: stringTable)
                        }
                    }
                    Spacer()
                }
            }
            .customNavigationTitle(with: NavigationScreens.dashboardFilter.localize(useTable: navigationStrings, withComment: "Select Dashboard Filter"))
        }
    }
}
