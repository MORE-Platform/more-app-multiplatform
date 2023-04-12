//
//  QuestionObservationView.swift
//  iosApp
//
//  Created by Isabella Aigner on 27.03.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import SwiftUI
import shared

struct QuestionObservationView: View {
    @Environment(\.presentationMode) var presentationMode
    @StateObject var viewModel: QuestionObservationViewModel = QuestionObservationViewModel()
    private let navigationStrings = "Navigation"
    
    @State private var selected = ""
    
    var body: some View {
        
        Navigation {
            MoreMainBackground {
                VStack {
                    Title2(titleText: .constant(viewModel.questionString))
                        .padding(.bottom, 20)
                        .padding(.top, 40)
                    
                    VStack(
                        alignment: .leading) {
                            ForEach(viewModel.answerOptions, id: \.self) { answerOption in
                                RadioButtonField(id: answerOption, label: answerOption, isMarked: $selected.wrappedValue == answerOption ? true : false,
                                                 callback: { selected in
                                    self.selected = selected
                                    print("Selected item is \(selected)")
                                })
                            }
                            
                            MoreActionButton(disabled: .constant(false)) {
                                if(self.selected != "") {
                                    self.presentationMode.wrappedValue.dismiss()
                                }
                                
                            } label: {
                                Text("Answer")
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
