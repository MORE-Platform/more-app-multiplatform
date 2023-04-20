//
//  SimpleQuetionObservationView.swift
//  iosApp
//
//  Created by Isabella Aigner on 27.03.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import SwiftUI
import shared

struct SimpleQuetionObservationView: View {
    @Environment(\.presentationMode) var presentationMode
    @EnvironmentObject var viewModel: SimpleQuestionObservationViewModel
    private let navigationStrings = "Navigation"
    private let simpleQuestionStrings = "SimpleQuestinoObservation"
    
    @State private var selected = ""
    
    var body: some View {
        
        Navigation {
            MoreMainBackground {
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

                                    print("------------------------")
                                    print("Selected item is \(selected)")
                                })
                            }
                             
                            MoreActionButton(disabled: .constant(false)) {
                                if(self.selected != "") {
                                    viewModel.finish()
                                    self.presentationMode.wrappedValue.dismiss()
                                }
                                
                            } label: {
                                Text(String.localizedString(forKey: "Answer", inTable: simpleQuestionStrings, withComment: "Click answer button to send your answer."))
                            }
                            .padding(.top, 30)
                        }
                        .padding(.horizontal, 10)
                    
            
                    Spacer()
                }
            }
            topBarContent: {
                EmptyView()
            }
            .customNavigationTitle(with: NavigationScreens.questionObservation.localize(useTable: navigationStrings, withComment: "Answer the Question Observation"))
            .navigationBarTitleDisplayMode(.inline)
        }
        
    }
}
