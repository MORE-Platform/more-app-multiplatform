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
    @StateObject var viewModel: NotificationFilterViewModel
    @State var filtersChanged = false
    private let stringTable = "NotificationView"
    private let navigationStrings = "Navigation"
    
    var body: some View {
        Navigation {
            MoreMainBackgroundView {
                VStack(alignment: .leading) {
                    VStack {
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
                }
                .onAppear {
                    viewModel.viewDidAppear()
                }
                .onDisappear {
                    viewModel.viewDidDisappear()
                }
            }
            .customNavigationTitle(with: NavigationScreens.notificationFilter.localize(useTable: navigationStrings, withComment: "Select Notification Filter"))
            .navigationBarTitleDisplayMode(.inline)
        }
    }
}

