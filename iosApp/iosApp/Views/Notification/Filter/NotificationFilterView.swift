//
//  NotificationFilterView.swift
//  More
//
//  Created by Mikolaj Luzak on 25.04.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import SwiftUI
import shared

struct NotificationFilterView: View {
    @EnvironmentObject var viewModel: NotificationFilterViewModel
    let stringTable = "NotifiationFilter"
    let navigationStrings = "Navigation"
    @State var filtersChanged = false
    
    var body: some View {
        Navigation {
            MoreMainBackgroundView {
                VStack(alignment: .leading) {
                    VStack {
                        MoreFilterOptionList(
                            title: .constant(String.localizedString(forKey: "Select Filter", inTable: stringTable, withComment: "Set Notification Filter")),
                            optionList: .constant(viewModel.allFilters),
                            selectedValueList: viewModel.currentFilters,
                            multiSelect: true,
                            updateFilters: viewModel.updateFilters,
                            isItemSelected: viewModel.isItemSelected,
                            stringTable: "NotificationFilter")
                    }.padding(.vertical, 20)
                    Spacer()
                }
            } topBarContent: {
                EmptyView()
            }
            .customNavigationTitle(with: NavigationScreens.notificationFilter.localize(useTable: navigationStrings, withComment: "Select Notification Filter"))
            .navigationBarTitleDisplayMode(.inline)
        }
    }
}

