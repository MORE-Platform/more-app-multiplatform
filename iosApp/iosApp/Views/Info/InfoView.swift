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
    let viewModel: InfoViewModel
    @EnvironmentObject private var navigationModalState: NavigationModalState

    var body: some View {
        ScrollView {
            Divider()
            VStack {
                InfoList()
                    .listRowSeparator(.hidden)
                    .listRowInsets(EdgeInsets())
                    .listRowBackground(Color.more.primaryLight)
                    .padding(.top, 7)
                Spacer()
            }
            .listStyle(.plain)
            .scrollContentBackground(.hidden)

            Spacer()

            if let id = viewModel.participantId, let alias = viewModel.participantAlias {
                HStack(alignment: .center) {
                    BasicText(text: "\("Participant") \(id): \(alias)", color: .more.secondary)
                }
                Divider()
            }

            ContactInfo(
                title: "info_contact_title",
                info: "info_disclaimer",
                contactInstitute: viewModel.contactInstitute,
                contactPerson: viewModel.contactPerson,
                contactEmail: viewModel.contactEmail,
                contactPhoneNumber: viewModel.contactPhoneNumber
            )

            Spacer()
            AppVersion()
        }
        .padding(.horizontal, 10)
        .customNavigationTitle(with: NavigationScreen.info.localize())
        .onAppear {
            viewModel.studyCoreModel.viewDidAppear()
        }
        .onDisappear {
            viewModel.studyCoreModel.viewDidDisappear()
        }
    }
}

struct InfoView_Previews: PreviewProvider {
    static var previews: some View {
        InfoView(viewModel: InfoViewModel())
            .environmentObject(ContentViewModel())
    }
}
