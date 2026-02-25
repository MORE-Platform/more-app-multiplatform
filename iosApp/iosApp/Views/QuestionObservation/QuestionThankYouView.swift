//
//  SimpleQuestionThankYouView.swift
//  More
//
//  Created by Julia Mayrhauser on 27.04.23.
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

struct QuestionThankYouView: View {
    @EnvironmentObject var questionModelState: NavigationModalState

    var body: some View {
        MoreMainBackgroundView {
            VStack(
                alignment: .leading,
                spacing: 10
            ) {
                Title2(titleText: "thank_you")
                    .padding(.top, 30)
                    .padding(.bottom, 10)
                BasicText(text: "answer_submitted", color: .more.secondary)
                    .padding(.bottom, 8)
                BasicText(text: "thank_you_participation", color: .more.secondary)
                Spacer()

                MoreActionButton(disabled: .constant(false)) {
                    questionModelState.closeView(screen: .questionObservationThanks)
                } label: {
                    BasicText(text: "Close", color: .more.white)
                }
                .padding(.bottom, 20)
            }
        }
        .navigationBarBackButtonHidden(true)
        .padding(.horizontal, 40)
        .customNavigationTitle(with: NavigationScreen.questionObservation.localize(), displayMode: .inline)
    }
}
