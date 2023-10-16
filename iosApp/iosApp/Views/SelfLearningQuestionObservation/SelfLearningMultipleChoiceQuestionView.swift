//
//  SelfLearningMultipleChoiceQuestionView.swift
//  More
//
//  Created by dhp lbi on 22.09.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import shared
import SwiftUI

struct SelfLearningMultipleChoiceQuestionView: View {
    @StateObject var viewModel: SelfLearningMultipleChoiceQuestionViewModel
    
    @EnvironmentObject var navigationModalState: NavigationModalState
    
    private let navigationStrings = "Navigation"
    private let selfLearningQuestionStrings = "SelfLearningMultipleChoiceQuestionObservation"

    @State private var selectedValues : Set<String> = []
    @State private var userTextAnswer : String = ""

    var body: some View {
        MoreMainBackgroundView {
            VStack {
                Title2(titleText: viewModel.selfLearningMultipleChoiceQuestionModel?.question ?? "Question")
                    .padding(.bottom, 20)
                    .padding(.top, 40)

                VStack(
                    alignment: .leading) {
                        ForEach(viewModel.answers, id: \.self) { answerOption in
                            CheckBoxField(id: answerOption, label: answerOption, isChecked: selectedValues.contains(answerOption)) { selected, isChecked in
                                if isChecked {
                                    self.selectedValues.insert(selected)
                                }else{
                                    self.selectedValues.remove(selected)
                                }
                                viewModel.setAnswer(answer: Array(selectedValues))
                            }
                        }
                        
                        VStack {
                            MoreTextFieldHL(isSmTextfield: .constant(false),
                                            headerText: "",
                                            inputPlaceholder: .constant("Enter your answer here"),
                                            input: $userTextAnswer,
                                            autoCorrectDisabled: true
                                            
                            )
                        }


                        VStack {
                            MoreActionButton(disabled: .constant(false)) {
                                if !self.userTextAnswer.isEmpty
                                {
                                    viewModel.userTextAnswer = userTextAnswer.trimmingCharacters(in: .whitespacesAndNewlines)
                                    viewModel.answerSet.append(viewModel.userTextAnswer)
                                }
                                if !viewModel.answerSet.isEmpty {
                                    viewModel.finish()
                                    navigationModalState.openView(screen: .selfLearningQuestionObservationThanks)
                                    navigationModalState.closeView(screen: .selfLearningQuestionObservation)
                                }
                            } label: {
                                Text(String.localize(forKey: "Answer", withComment: "Click answer button to send your answer.", inTable: selfLearningQuestionStrings))
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
        .customNavigationTitle(with: NavigationScreens.selfLearningQuestionObservation.localize(useTable: navigationStrings, withComment: "Answer the Self Learning Question Observation"), displayMode: .inline)
    }
}
