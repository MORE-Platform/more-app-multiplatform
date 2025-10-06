//
//  ExitStudyLevelOneView.swift
//  iosApp
//
//  Created by Daniil Barkov on 13.04.23.
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

struct LeaveStudyView: View {
    @StateObject private var viewModel: SettingsViewModel = SettingsViewModel()
    @EnvironmentObject var contentViewModel: ContentViewModel
    @EnvironmentObject private var navigationModalState: NavigationModalState

    @State var accButton = Color.more.approved
    @State var decButton = Color.more.important

    var body: some View {
        MoreMainBackgroundView {
            VStack(alignment: .center) {
                Title2(titleText: viewModel.studyTitle ?? "")
                    .padding(.vertical)
                    .multilineTextAlignment(.center)
                    .frame(maxWidth: .infinity)

                Spacer()

                Image(systemName: "exclamationmark.triangle.fill")
                    .font(.system(size: 60))
                    .foregroundColor(Color.more.important)
                    .padding()

                SectionHeading(sectionTitle: "first_message")
                    .foregroundColor(Color.more.important)
                    .padding(.bottom, 2)
                    .multilineTextAlignment(.center)

                Spacer()

                Text("really_message")
                    .padding(.bottom)

                MoreActionButton(
                    backgroundColor: .more.approved,
                    disabled: .constant(false)
                ) {
                    navigationModalState.closeView(screen: .withdrawStudy)
                } label: {
                    Text("continue_study").foregroundColor(Color.more.white)
                }
                .padding(.bottom, 2)

                MoreActionButton(
                    backgroundColor: .more.important,
                    disabled: .constant(false)
                ) {
                    navigationModalState.openView(screen: .withdrawStudyConfirm)
                } label: {
                    Text("withdraw_study").foregroundColor(Color.more.white)
                }

                Spacer()
            }
            .padding(.horizontal, 40)
        }
        .fullScreenCover(isPresented: navigationModalState.screenBinding(for: .withdrawStudyConfirm)) {
            LeaveStudyConfirmationView(viewModel: viewModel)
        }
        .customNavigationTitle(with: NavigationScreen.withdrawStudy.localize())
    }
}
