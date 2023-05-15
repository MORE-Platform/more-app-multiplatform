//
//  SimpleQuestionThankYouView.swift
//  More
//
//  Created by Julia Mayrhauser on 27.04.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import SwiftUI

struct SimpleQuestionThankYouView: View {
    @EnvironmentObject var questionModelState: QuestionModalState
    private let navigationStrings = "Navigation"
    private let simpleQuestionStrings = "SimpleQuestionObservation"
    
    var body: some View {
        Navigation {
            MoreMainBackgroundView {
                VStack {
                    VStack(
                        alignment: .leading,
                        spacing: 10
                    ) {
                        Title2(titleText: .constant(String.localizedString(forKey: "thank_you", inTable: simpleQuestionStrings, withComment: "Thank You!")))
                            .padding(.top, 30)
                            .padding(.bottom, 10)
                        BasicText(text: .constant(String.localizedString(forKey: "answer_submitted", inTable: simpleQuestionStrings, withComment: "Your answer was submitted!")), color: .more.secondary)
                            .padding(.bottom, 8)
                        BasicText(text: .constant(String.localizedString(forKey: "thank_you_participation", inTable: simpleQuestionStrings, withComment: "Thanks for your participation!")), color: .more.secondary)
                        Spacer()
                        
                        MoreActionButton(disabled: .constant(false)) {
                            questionModelState.simpleQuestionThankYouOpen = false
                        } label: {
                            BasicText(text: .constant(String.localizedString(forKey: "close", inTable: simpleQuestionStrings, withComment: "Close")), color: .more.white)
                        }
                        .padding(.bottom, 20)
                    }
                    .navigationBarBackButtonHidden(true)
                }
                .padding(.horizontal, 40)
            }
            .customNavigationTitle(with: NavigationScreens.questionObservation.localize(useTable: navigationStrings, withComment: "Thank you for answering the Question Observation"))
            .navigationBarTitleDisplayMode(.inline)
            }
        }
    }

