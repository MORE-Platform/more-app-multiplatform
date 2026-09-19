//
//  ExitStudyLevelTwoView.swift
//  iosApp
//
//  Created by Daniil Barkov on 13.04.23.
//  Copyright © 2023 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung
//  Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
//

import SwiftUI

struct LeaveStudyConfirmationView: View {
    @StateObject var viewModel: SettingsViewModel
    @EnvironmentObject private var navigationModalState: NavigationModalState

    @State private var simpleRightDirectionSliderOffsetX: CGFloat = 0
    @State private var simpleLeftDirectionSliderOffsetX: CGFloat = 0
    @State private var rectangularSliderOffsetX: CGFloat = 0
    @State private var neumorphicSliderOffsetX: CGFloat = 0
    @State private var alertPresented: Bool = false
    @State var continueButton = Color.more.approved

    var body: some View {
        MoreMainBackgroundView {
            VStack {
                Title2(titleText: viewModel.studyTitle ?? "")
                    .padding(.vertical)
                    .multilineTextAlignment(.center)
                    .frame(maxWidth: .infinity)

                Spacer()

                Image(systemName: "exclamationmark.triangle.fill")
                    .font(.system(size: 60))
                    .foregroundColor(Color.more.important)
                    .padding()

                Text("leave_confirmation_message")
                    .foregroundColor(Color.more.secondary)
                    .padding(.bottom, 2)
                    .multilineTextAlignment(.center)

                Text("sure_message")
                    .foregroundColor(Color.more.primary)
                    .fontWeight(.bold)
                    .padding(.bottom, 2)
                    .multilineTextAlignment(.center)

                Spacer()
                    .frame(height: 150)

                MoreActionButton(
                    backgroundColor: .more.approved,
                    disabled: .constant(false)
                ) {
                    navigationModalState.closeView(screen: .withdrawStudy)
                    navigationModalState.closeView(screen: .withdrawStudyConfirm)
                } label: {
                    Text("continue_study").foregroundColor(Color.more.white)
                }
                .padding(.bottom, 2)

                MoreActionButton(backgroundColor: .more.important, disabled: .constant(false)) {
                    viewModel.leaveStudy()
                    navigationModalState.clearViews()
                } label: {
                    Text("withdraw")
                }

                Spacer()
            }
            .padding(.horizontal, 40)
        }
        .customNavigationTitle(with: NavigationScreen.settings.localize())
    }
}
