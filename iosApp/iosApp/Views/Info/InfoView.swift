//
//  InfoView.swift
//  iosApp
//
//  Created by Jan Cortiel on 14.03.23.
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

struct InfoView: View {
    @EnvironmentObject var contentViewModel: ContentViewModel
    @StateObject var viewModel: InfoViewModel
    private let navigationStrings = "Navigation"
    private let infoStrings = "Info"
    var body: some View {
        Navigation {
            MoreMainBackgroundView {
                ScrollView {
                    VStack {
                        Divider()
                        VStack {
                            InfoList()
                                .fullScreenCover(isPresented: $contentViewModel.isLeaveStudyOpen) {
                                    LeaveStudyView(viewModel: contentViewModel.settingsViewModel)
                                        .environmentObject(contentViewModel)
                                }
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
                        
                        if let id = viewModel.participantId, let alias = viewModel.participantAlias {
                            HStack(alignment: .center) {
                                BasicText(text: "\("Participant".localize(withComment: "Participant ID", useTable: infoStrings)) \(id): \(alias)", color: .more.secondary)
                            }
                            Divider()
                        }
                        
                        ContactInfo(
                            title: String.localize(forKey: "info_contact_title", withComment: "Contact us.", inTable: infoStrings),
                            info: String.localize(forKey: "info_disclaimer", withComment: "Contact us.", inTable: infoStrings),
                            contactInstitute: viewModel.contactInstitute,
                            contactPerson: viewModel.contactPerson,
                            contactEmail: viewModel.contactEmail,
                            contactPhoneNumber: viewModel.contactPhoneNumber
                        )
                        
                        Spacer()
                        AppVersion()
                    }
                }
                .padding(.horizontal, 10)
            } 
            .customNavigationTitle(with: NavigationScreens.info.localize(useTable: navigationStrings, withComment: "Information Title"))
            .onAppear {
                viewModel.viewDidAppear()
            }
            .onDisappear {
                viewModel.viewDidDisappear()
            }
        }
        
    }
}

struct InfoView_Previews: PreviewProvider {
    static var previews: some View {
        InfoView(viewModel: InfoViewModel())
            .environmentObject(ContentViewModel())
    }
}
