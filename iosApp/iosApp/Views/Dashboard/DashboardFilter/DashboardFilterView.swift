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
    

    
    @State var dateFilter: DateFilterModel = DateFilterModel.entireTime
    @State var observationTypeFilter: [String] = ["GPS Mobile Sensor", "Lime Survey"]
    
    var body: some View {
        Navigation {
            MoreMainBackgroundView {
                VStack(alignment: .leading) {
                    VStack {
                        VStack {
                            /*MoreFilterOptionList(
                                            title: .constant(String.localizedString(forKey: "Set duration", inTable: stringTable, withComment: "Set the duration for your dashboard view.")),
                                            selectOptionList: .constant( dashboardFilterViewModel.dateFilterOptions),
                                            selectedValue: dashboardFilterViewModel.dateFilter,
                                            optionCallback: { filter, bool in
                                                     print(filter)
                                                     print(bool)

                                                     if bool {
                                                         //dashboardFilterViewModel.coreModel.setDateFilter(filter)
                                                         self.dateFilter = filter
                                                     } else {
                                                         /*dashboardFilterViewModel.coreModel.setDateFilter(DateFilterModul.TODAY_AND_TOMORROW)*/
                                                         self.dateFilter = DateFilterModel.values()
                                                     }

                                                 }
                            )
                                .padding(.vertical, 25)*/
                            
                            //Text(dashboardFilterViewModel.dateFilterList)
                            /*ForEach(dashboardFilterViewModel.typeFilterList, id: \.self) { filter in
                                Text(filter)
                            }*/
                        }
                        
                        VStack {
                            
                            MoreFilterOptionList(
                                multiSelect: true,
                                title: .constant(String.localizedString(forKey: "Set observation type", inTable: stringTable, withComment: "Set filter on what observation type should be shown in dashboard view.")),
                                selectOptionList: .constant(dashboardFilterViewModel.typeFilterList),
                                selectedValueList: observationTypeFilter,
                                optionCallback: { filter, bool in
                                    if bool {
                                        //dashboardFilterViewModel.coreModel.addTypeFilter(filter)
                                        //self.observationTypeFilter.insert(filter)
                                    } else {
                                        //dashboardFilterViewModel.coreModel.removeTypeFilter(filter)
                                        //self.observationTypeFilter.remove(filter)
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
