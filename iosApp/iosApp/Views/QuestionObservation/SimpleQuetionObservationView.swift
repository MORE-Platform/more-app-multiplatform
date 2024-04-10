//
//  SimpleQuetionObservationView.swift
//  iosApp
//
//  Created by Isabella Aigner on 27.03.23.
//  Copyright © 2023 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung 
//  Licensed under the Apache 2.0 license with Commons Clause 
//  (see https://www.apache.org/licenses/LICENSE-2.0 and
//  https://commonsclause.com/).
//

import shared
import SwiftUI

struct SimpleQuetionObservationView: View {
    @StateObject var viewModel: SimpleQuestionObservationViewModel

    @EnvironmentObject var navigationModalState: NavigationModalState

    private let navigationStrings = "Navigation"
    private let simpleQuestionStrings = "SimpleQuestinoObservation"

    var body: some View {
        Navigation {
            MoreMainBackgroundView {
                VStack {
                    Title2(titleText: viewModel.simpleQuestoinModel?.question ?? "Question")
                        .padding(.bottom, 20)
                        .padding(.top, 40)

                    VStack(
                        alignment: .leading) {
                            ForEach(viewModel.answers, id: \.self) { answerOption in
                                RadioButtonField(id: answerOption, label: answerOption, isMarked: viewModel.answerSet == answerOption ? true : false,
                                                 callback: { selected in
                                                     viewModel.setAnswer(answer: selected)
                                                 })
                            }

                            VStack {
                                MoreActionButton(disabled: .constant(viewModel.answerSet.isEmpty)) {
                                    if !self.viewModel.answerSet.isEmpty {
                                        viewModel.finish()
                                        navigationModalState.openView(screen: .questionObservationThanks)
                                        navigationModalState.closeView(screen: .questionObservation)
                                    }
                                } label: {
                                    Text(String.localize(forKey: "Answer", withComment: "Click answer button to send your answer.", inTable: simpleQuestionStrings))
                                }
                                .padding(.top, 30)
                            }
                        }
                        .padding(.horizontal, 10)
                        .onAppear {
                            viewModel.viewDidAppear()
                        }
                        .onDisappear {
                            viewModel.viewDidDisappear()
                        }
                    Spacer()
                }
            }
            .customNavigationTitle(with: NavigationScreens.questionObservation.localize(useTable: navigationStrings, withComment: "Answer the Question Observation"), displayMode: .inline)
            .toolbar {
                ToolbarItem(placement: .confirmationAction) {
                    Button {
                        navigationModalState.closeView(screen: .questionObservation)
                    } label: {
                        Image(systemName: "chevron.down")
                            .foregroundColor(.more.important)
                    }
                }
            }
        }
    }
}
