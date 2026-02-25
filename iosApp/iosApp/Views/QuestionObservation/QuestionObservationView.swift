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

struct QuestionObservationView: View {
    @StateObject private var viewModel: QuestionViewModel

    @EnvironmentObject private var navigationModalState: NavigationModalState

    @State private var singleSelected: String? = nil
    @State private var multiSelected: Set<String> = []

    init(navigationState: NavigationState) {
        _viewModel = StateObject(wrappedValue: QuestionViewModel(navigationState: navigationState))
    }

    var body: some View {
        MoreMainBackgroundView {
            VStack {
                Title2(titleText: viewModel.questionModel?.question ?? "Question")
                    .padding(.bottom, 20)
                    .padding(.top, 40)

                VStack(alignment: .leading) {
                    if viewModel.questionModel?.type == .singleChoice {
                        SingleChoiceView(viewModel: viewModel, selected: $singleSelected)
                    } else if viewModel.questionModel?.type == .multipleChoice {
                        MultiChoiceView(viewModel: viewModel, selected: $multiSelected)
                    }

                    VStack {
                        MoreActionButton(disabled: .constant(answerEntered())) {
                            switch viewModel.questionModel?.type {
                            case .singleChoice:
                                if let selected = singleSelected {
                                    viewModel.finish(data: selected as NSString)
                                }
                            case .multipleChoice:
                                if !multiSelected.isEmpty {
                                    viewModel.finish(data: multiSelected.map { $0 as NSString } as NSArray)
                                }
                            default:
                                break
                            }
                            navigationModalState.openView(screen: .questionObservationThanks)
                            navigationModalState.closeView(screen: .questionObservation)
                        } label: {
                            Text("Answer")
                        }
                        .padding(.top, 30)
                    }
                }
                .padding(.horizontal, 10)
                .onAppear {
                    singleSelected = nil
                    multiSelected.removeAll()
                    viewModel.viewDidAppear()
                }
                .onDisappear {
                    viewModel.viewDidDisappear()
                }
                Spacer()
            }
        }
        .customNavigationTitle(with: NavigationScreen.questionObservation.localize(), displayMode: .inline)
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
    
    private func answerEntered() -> Bool {
        (viewModel.questionModel?.type == .singleChoice && singleSelected == nil) || (viewModel.questionModel?.type == .multipleChoice && multiSelected.isEmpty)
    }
}

