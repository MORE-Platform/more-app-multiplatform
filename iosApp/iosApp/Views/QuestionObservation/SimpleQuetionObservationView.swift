//
//  SimpleQuetionObservationView.swift
//  iosApp
//
//  Created by Isabella Aigner on 27.03.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import shared
import SwiftUI

struct SimpleQuetionObservationView: View {
    var scheduleId: String

    @EnvironmentObject var questionModalState: QuestionModalState
    @StateObject var viewModel: SimpleQuestionObservationViewModel = SimpleQuestionObservationViewModel()
    private let navigationStrings = "Navigation"
    private let simpleQuestionStrings = "SimpleQuestinoObservation"

    @State private var selected = ""

    var body: some View {
        MoreMainBackgroundView {
            VStack {
                Title2(titleText: .constant(viewModel.simpleQuestoinModel?.question ?? "Question"))
                    .padding(.bottom, 20)
                    .padding(.top, 40)

                VStack(
                    alignment: .leading) {
                        ForEach(viewModel.answers, id: \.self) { answerOption in
                            RadioButtonField(id: answerOption, label: answerOption, isMarked: $selected.wrappedValue == answerOption ? true : false,
                                             callback: { selected in
                                                 self.selected = selected
                                                 viewModel.setAnswer(answer: selected)
                                             })
                        }

                        VStack {
                            MoreActionButton(disabled: .constant(false)) {
                                if self.selected != "" {
                                    viewModel.finish()
                                    questionModalState.simpleQuestionThankYouOpen = true
                                    questionModalState.simpleQuestionOpen = false
                                }
                            } label: {
                                Text(String.localizedString(forKey: "Answer", inTable: simpleQuestionStrings, withComment: "Click answer button to send your answer."))
                            }
                            .padding(.top, 30)
                            .fullScreenCover(isPresented: $questionModalState.simpleQuestionThankYouOpen) {
                                SimpleQuestionThankYouView()
                                    .environmentObject(questionModalState)
                            }
                        }
                    }
                    .padding(.horizontal, 10)
                    .onAppear {
                        viewModel.viewDidAppear(scheduleId: scheduleId)
                    }
                Spacer()
            }
        }
        .customNavigationTitle(with: NavigationScreens.questionObservation.localize(useTable: navigationStrings, withComment: "Answer the Question Observation"))
        .navigationBarTitleDisplayMode(.inline)
    }
}
