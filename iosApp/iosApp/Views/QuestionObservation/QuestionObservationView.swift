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
import UIKit

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
                            // Prepare data to submit based on the question type
                            let dataToSubmit: AnyObject? = {
                                switch viewModel.questionModel?.type {
                                case .singleChoice:
                                    if let selected = singleSelected { return selected as NSString }
                                case .multipleChoice:
                                    if !multiSelected.isEmpty { return multiSelected.map { $0 as NSString } as NSArray }
                                default:
                                    break
                                }
                                return nil
                            }()

                            // Navigate immediately for responsiveness
                            navigationModalState.openView(screen: .questionObservationThanks)
                            navigationModalState.closeView(screen: .questionObservation)

                            // Perform submission off the main thread to avoid blocking UI
                            if let data = dataToSubmit {
                                DispatchQueue.global(qos: .userInitiated).async {
                                    viewModel.finish(data: data)
                                }
                            }
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
                    prewarmSymbols()
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
    
    private func prewarmSymbols() {
        // Preload SF Symbols used in answer rows to avoid first-tap lag
        _ = UIImage(systemName: "largecircle.fill.circle")
        _ = UIImage(systemName: "circle")
        _ = UIImage(systemName: "checkmark.square.fill")
        _ = UIImage(systemName: "square")
    }
    
    private func answerEntered() -> Bool {
        (viewModel.questionModel?.type == .singleChoice && singleSelected == nil) || (viewModel.questionModel?.type == .multipleChoice && multiSelected.isEmpty)
    }
}

