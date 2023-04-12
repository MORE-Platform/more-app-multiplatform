//
//  InfoView.swift
//  iosApp
//
//  Created by Jan Cortiel on 14.03.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import SwiftUI

struct InfoView: View {
    @EnvironmentObject var contentViewModel: ContentViewModel
    private let navigationStrings = "Navigation"
    var body: some View {
        Navigation {
            MoreMainBackgroundView {
                VStack {
                    Divider()
                    VStack {
                        InfoList()
                        .hideListRowSeparator()
                        .listRowInsets(EdgeInsets())
                        .listRowBackground(Color.more.primaryLight)
                        Spacer()
                    }
                    .listStyle(.plain)
                    .clearListBackground()
                }
                .padding(.horizontal, 24)
            } topBarContent: {
                EmptyView()
            }
            .customNavigationTitle(with: NavigationScreens.info.localize(useTable: navigationStrings, withComment: "Information Title"))
            .navigationBarTitleDisplayMode(.inline)
        }
        
    }
}

struct InfoView_Previews: PreviewProvider {
    static var previews: some View {
        InfoView()
            .environmentObject(ContentViewModel())
    }
}
