//
//  SelfLearningMultipleChoiceQuestionThankYouView.swift
//  More
//
//  Created by dhp lbi on 22.09.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import SwiftUI

struct SelfLearningMultipleChoiceQuestionThankYouView: View {
    @EnvironmentObject var questionModelState: NavigationModalState
    private let navigationStrings = "Navigation"
    private let selfLearningQuestionStrings = "SimpleQuestionObservation"
    
    var body: some View {
        Navigation {
            MoreMainBackgroundView {
                VStack {
                    VStack(
                        alignment: .leading,
                        spacing: 10
                    ) {
                        Title2(titleText: String.localize(forKey: "thank_you", withComment: "Thank You!", inTable: selfLearningQuestionStrings))
                            .padding(.top, 30)
                            .padding(.bottom, 10)
                        BasicText(text: String.localize(forKey: "answer_submitted", withComment: "Your answer was submitted!", inTable: selfLearningQuestionStrings), color: .more.secondary)
                            .padding(.bottom, 8)
                        BasicText(text: String.localize(forKey: "thank_you_participation", withComment: "Thanks for your participation!", inTable: selfLearningQuestionStrings), color: .more.secondary)
                        Spacer()
                        
                        MoreActionButton(disabled: .constant(false)) {
                            questionModelState.selfLearningQuestionThankYouOpen = false
                        } label: {
                            BasicText(text: String.localize(forKey: "close", withComment: "Close", inTable: selfLearningQuestionStrings), color: .more.white)
                        }
                        .padding(.bottom, 20)
                    }
                    .navigationBarBackButtonHidden(true)
                }
                .padding(.horizontal, 40)
            }
            .customNavigationTitle(with: NavigationScreens.selfLearningQuestionObservation.localize(useTable: navigationStrings, withComment: "Thank you for answering the Question Observation"), displayMode: .inline)
            }
        }
    }
