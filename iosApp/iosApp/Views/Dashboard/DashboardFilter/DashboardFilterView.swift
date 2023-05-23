//
//  DashboardFilterView.swift
//  iosAp.p
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
                            SectionHeading(sectionTitle: .constant(String.localizedString(forKey: "Select Time", inTable: stringTable, withComment: "Set time filter")))
                                .padding(15)
                            Divider()
                            
                            ForEach(viewModel.currentDateFilter.keys.sorted{$0.sortIndex < $1.sortIndex}, id: \.self) { filter in
                                if let selected = viewModel.currentDateFilter[filter]?.boolValue {
                                    Button {
                                        viewModel.toggleDateFilter(dateFilter: filter)
                                    } label: {
                                        HStack {
                                            MoreFilterOption(option: filter.describing, isSelected: .constant(selected))
                                            Spacer()
                                        }
                                    }
                                    .buttonStyle(.borderless)
                                    .frame(maxWidth: .infinity)
                                    Divider()
                                }
                            }
                        }.padding(.vertical, 20)
                        
                        VStack {
                            SectionHeading(sectionTitle: .constant(String.localizedString(forKey: "Select Type", inTable: stringTable, withComment: "Set titypeme filter")))
                                .padding(15)
                            Divider()
                            
                            Button{
                                viewModel.clearTypeFilter()
                            } label: {
                                HStack {
                                    MoreFilterOption(option: String.localizedString(forKey: "All Items", inTable: stringTable, withComment: "String for All Items"), isSelected: $viewModel.typeFilterActive)
                                    Spacer()
                                }
                            }
                            .buttonStyle(.borderless)
                            .frame(maxWidth: .infinity)
                            
                            Divider()
                            ForEach(viewModel.currentTypeFilter.keys.sorted(), id: \.self) { filter in
                                if let selected = viewModel.currentTypeFilter[filter]?.boolValue {
                                    Button{
                                        viewModel.toggleTypeFilter(type: filter)
                                    } label: {
                                        HStack {
                                            MoreFilterOption(option: filter, isSelected: .constant(selected))
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
                    Spacer()
                }
            }
            .customNavigationTitle(with: NavigationScreens.dashboardFilter.localize(useTable: navigationStrings, withComment: "Select Dashboard Filter"))
        }
    }
}
