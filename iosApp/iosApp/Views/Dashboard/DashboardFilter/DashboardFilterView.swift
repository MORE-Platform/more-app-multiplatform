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
    
    @State var dateFilter: String = String(describing: DateFilterModel.todayAndTomorrow)
    @State var observationTypeFilter: [String] = ["Today and Tomorrow"]
    
    var body: some View {
        Navigation {
            MoreMainBackgroundView {
                VStack(alignment: .leading) {
                    VStack {
                        VStack {
                            
                            MoreFilterOptionList(
                                            title: .constant(String.localizedString(forKey: "Set duration", inTable: stringTable, withComment: "Set the duration for your dashboard view.")),
                                            optionList: .constant( dashboardFilterViewModel.dateFilterStringList),
                                            selectedValue: String(describing: dashboardFilterViewModel.dateFilter),
                                            optionCallback: { filter, bool in
                                                    if bool {
                                                        /*dashboardFilterViewModel.coreModel.setDateFilter(dateFilter:
                                                            dashboardFilterViewModel.getDateFilterValue(filter: filter)
                                                        )*/
                                                    } else {
                                                            //dashboardFilterViewModel.coreModel.setDateFilter(dateFilter: DateFilterModel.todayAndTomorrow)
                                                        
                                                     }
                                            }
                            )
                                .padding(.vertical, 25)
                        }
                        
                        VStack {
                            
                            MoreFilterOptionList(
                                multiSelect: true,
                                title: .constant(String.localizedString(forKey: "Set observation type", inTable: stringTable, withComment: "Set filter on what observation type should be shown in dashboard view.")),
                                optionList: .constant(dashboardFilterViewModel.observationTypeFilterList),
                                selectedValueList: observationTypeFilter,
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
                            /*SectionHeading(sectionTitle: .constant(String.localizedString(forKey: "Set observation type", inTable: stringTable, withComment: "Set filter on what observation type should be shown in dashboard view.")))*/
                        }
                        
                        /*MoreFilterOptionList(title: .constant(String.localizedString(forKey: "Set duration", inTable: stringTable, withComment: "Set the duration of the schedule time frame.")), filterList: .constant(dashboardFilterViewModel.dateFilterOptions))
                         }*/
                    }
                    Spacer()
                }
            } topBarContent: {
                EmptyView()
            }
            .customNavigationTitle(with: NavigationScreens.dashboardFilter.localize(useTable: navigationStrings, withComment: "Select Dashboard Filter"))
                .navigationBarTitleDisplayMode(.inline)
        }
    }
}
