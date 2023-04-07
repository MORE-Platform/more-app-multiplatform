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
                                stringTable: .constant("DashboardFilter"),
                                title: .constant(String.localizedString(forKey: "Set duration", inTable: stringTable, withComment: "Set the duration for your dashboard view.")),
                                optionList: .constant( dashboardFilterViewModel.dateFilterStringList),
                                selectedValue: String(describing: dashboardFilterViewModel.dateFilter),
                                optionCallback:
                                            { filter, bool in
                                                print("------------single-------------")
                                    if bool {
                                        print("true")
                                        print(filter)
                                        dashboardFilterViewModel.setDateFilterValue(filter: filter)
                                    } else {
                                        print("true")
                                        dashboardFilterViewModel.coreModel.setDateFilter(dateFilter: DateFilterModel.todayAndTomorrow)
                                    }
                            })
                            .padding(.vertical,25)
                        }
                        
                        VStack {
                            MoreFilterMultiOptionList(
                                stringTable: .constant("ObservationTypes"), title: .constant(String.localizedString(forKey: "Set observation type", inTable: stringTable, withComment: "Set filter on what observation type should be shown in dashboard view.")),
                                optionList: .constant(dashboardFilterViewModel.observationTypeFilterList),
                                selectedValueList: dashboardFilterViewModel.observationTypeFilter,
                                optionCallback: { filter, bool in
                                    print("------------multi-------------")
                                    if bool {
                                        print("true")
                                        print(filter)
                                        dashboardFilterViewModel.coreModel.addTypeFilter(type: filter)
                                    } else {
                                        if (filter == "reset") {
                                            print("reset")
                                            print(filter)
                                            dashboardFilterViewModel.coreModel.clearTypeFilters()
                                        } else {
                                            print("false")
                                            print(filter)
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
