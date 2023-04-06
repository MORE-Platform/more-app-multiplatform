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
    @StateObject var dashboardFilterViewModel: DashboardFilterViewModel
    let stringTable = "DashboardFilter"
    let navigationStrings = "Navigation"
    
    var body: some View {
        Navigation {
            MoreMainBackgroundView {
                VStack(alignment: .leading) {
                    VStack {
                        VStack {
                            
                            MoreFiltreSingleOptionList(
                                title: .constant(String.localizedString(forKey: "Set duration", inTable: stringTable, withComment: "Set the duration for your dashboard view.")),
                                optionList: .constant( dashboardFilterViewModel.dateFilterStringList),
                                selectedValue: String(describing: dashboardFilterViewModel.dateFilter),
                                optionCallback:
                                            { filter, bool in
                                    if bool {
                                        dashboardFilterViewModel.setDateFilterValue(filter: filter)
                                    } else {
                                        dashboardFilterViewModel.coreModel.setDateFilter(dateFilter: DateFilterModel.todayAndTomorrow)
                                        dashboardFilterViewModel.setCurrentFilters()
                                    }
                            })
                            .padding(.vertical,25)
                        }
                        
                        VStack {
                            MoreFilterMultiOptionList(
                                title: .constant(String.localizedString(forKey: "Set observation type", inTable: stringTable, withComment: "Set filter on what observation type should be shown in dashboard view.")),
                                optionList: .constant(dashboardFilterViewModel.observationTypeFilterList),
                                selectedValueList: dashboardFilterViewModel.observationTypeFilter,
                                optionCallback: { filter, bool in
                                    if bool {
                                        dashboardFilterViewModel.coreModel.addTypeFilter(type: filter)
                                    } else {
                                        if (filter == "reset") {
                                            dashboardFilterViewModel.coreModel.clearTypeFilters()
                                        } else {
                                            dashboardFilterViewModel.coreModel.removeTypeFilter(type: filter)
                                        }
                                    }
                                }
                            )
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
