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
    @EnvironmentObject var dashboardFilterViewModel: DashboardFilterViewModel
    let stringTable = "DashboardFilter"
    let navigationStrings = "Navigation"
    
    var body: some View {
        Navigation {
            MoreMainBackgroundView {
                VStack(alignment: .leading) {
                    VStack {
                        VStack {
                            MoreFilterOptionList(multiSelect: false, title: .constant("Set Time"), optionList: .constant(dashboardFilterViewModel.dateFilterStringList), selectedValueList: [""], optionCallback: { filter, bool  in
                                dashboardFilterViewModel.setDateFilterValue(filter: filter)
                            })
                            .padding(.vertical,25)
                        }
                        
                        VStack {
                            MoreFilterOptionList(multiSelect: true, title: .constant("Set Observation Type"), optionList: .constant(dashboardFilterViewModel.observationTypeFilterList), selectedValueList: [""], optionCallback: { filter, bool  in
                                
                            })
                            .padding(.vertical,25)
                        }
                    }
                    Spacer()
                }
            } topBarContent: {
                EmptyView()
            }.onAppear {
                dashboardFilterViewModel.setCurrentFilters()
            }
            .customNavigationTitle(with: NavigationScreens.dashboardFilter.localize(useTable: navigationStrings, withComment: "Select Dashboard Filter"))
            .navigationBarTitleDisplayMode(.inline)
        }
    }
}
