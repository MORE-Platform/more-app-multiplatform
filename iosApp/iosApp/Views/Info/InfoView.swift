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
    @StateObject var viewModel: InfoViewModel
    private let navigationStrings = "Navigation"
    private let infoStrings = "Info"
    var body: some View {
        Navigation {
            MoreMainBackgroundView {
                VStack {
                    Divider()
                    VStack {
                        InfoList()
                            .environmentObject(contentViewModel)
                        .hideListRowSeparator()
                        .listRowInsets(EdgeInsets())
                        .listRowBackground(Color.more.primaryLight)
                        .padding(.top, 7)
                        Spacer()
                    }
                    .listStyle(.plain)
                    .clearListBackground()
                    
                    Spacer()
                    ScrollView {
                        ContactInfo(
                            studyTitle: .constant(viewModel.studyTitle  ?? ""),
                            institute: .constant(viewModel.institute),
                            contactPerson: .constant(viewModel.contactPerson),
                            info: .constant(String.localizedString(forKey: "info_disclaimer", inTable: infoStrings, withComment: "Contact us.")),
                            contactEmail: viewModel.contactEmail,
                            contactTel: viewModel.contactTel
                        )
                    }
                    Spacer()
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
        InfoView(viewModel: InfoViewModel())
            .environmentObject(ContentViewModel())
    }
}
