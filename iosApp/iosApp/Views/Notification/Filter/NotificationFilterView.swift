//
//  NotificationFilterView.swift
//  More
//
//  Created by Mikolaj Luzak on 25.04.23.
//  Copyright © 2023 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung 
//  Licensed under the Apache 2.0 license with Commons Clause 
//  (see https://www.apache.org/licenses/LICENSE-2.0 and
//  https://commonsclause.com/).
//

import SwiftUI
import shared

struct NotificationFilterView: View {
    @StateObject var viewModel: NotificationFilterViewModel
    @State var filtersChanged = false
    private let stringTable = "NotificationView"
    private let navigationStrings = "Navigation"
    
    var body: some View {
        VStack(alignment: .leading) {
            ScrollView {
                SectionHeading(sectionTitle: String.localize(forKey: "Select Filter", withComment: "Set Notification Filter", inTable: stringTable))
                    .padding(15)
                Divider()
                
                ForEach(viewModel.allFilters.keys.sorted{$0.sortIndex < $1.sortIndex}, id: \.self) { filter in
                    if let selected = viewModel.allFilters[filter]?.boolValue {
                        Button {
                            viewModel.toggleFilters(filter: filter)
                        } label: {
                            MoreFilterOption(option: filter.type.localize(withComment: filter.type, useTable: stringTable), isSelected: .constant(selected))
                            Spacer()
                        }
                        .buttonStyle(.borderless)
                        .frame(maxWidth: .infinity)
                        Divider()
                        
                    }
                }
            }.padding(.vertical, 20)
            Spacer()
        }
        .onAppear {
            viewModel.viewDidAppear()
        }
        .onDisappear {
            viewModel.viewDidDisappear()
        }
        .customNavigationTitle(with: NavigationScreen.notificationFilter.localize(useTable: navigationStrings, withComment: "Select Notification Filter"))
        .navigationBarTitleDisplayMode(.inline)
    }
}

