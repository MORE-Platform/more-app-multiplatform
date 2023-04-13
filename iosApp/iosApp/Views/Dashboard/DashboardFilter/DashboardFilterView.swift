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
                            MoreFilterOptionList(multiSelect: false, title: .constant("Set Time"), optionList: .constant(viewModel.dateFilterStringList), selectedValueList: [viewModel.dateFilter.name], optionCallback: { filters  in
                                if !filters.isEmpty {
                                    viewModel.dateFilterString = filters[0]
                                }
                            })
                            .padding(.vertical,25)
                        }
                        
                        VStack {
                            MoreFilterOptionList(multiSelect: true, title: .constant("Set Observation Type"), optionList: .constant(viewModel.observationTypes), selectedValueList: viewModel.observationTypeFilter, optionCallback: { filters  in
                                viewModel.observationTypeFilter = filters
                            })
                            .padding(.vertical,25)
                        }
                    }
                    Spacer()
                }
            } topBarContent: {
                EmptyView()
            }
            .customNavigationTitle(with: NavigationScreens.dashboardFilter.localize(useTable: navigationStrings, withComment: "Select Dashboard Filter"))
            .navigationBarTitleDisplayMode(.inline)
            .onAppear {
                viewModel.setCurrentFilters()
            }
        }
    }
}
