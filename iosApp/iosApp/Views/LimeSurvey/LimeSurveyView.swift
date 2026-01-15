//
//  LimeSurveyView.swift
//  More
//
//  Created by Jan Cortiel on 11.05.23.
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

struct LimeSurveyView: View {
    @StateObject private var viewModel: LimeSurveyViewModel
    
    init(navigationState: NavigationState) {
        _viewModel = StateObject(wrappedValue: LimeSurveyViewModel(navigationState: navigationState))
    }

    var body: some View {
        MoreMainBackgroundView(contentPadding: 0) {
            VStack {
                if viewModel.dataLoading {
                    HStack {
                        Text("Data is loading...")
                    }
                } else if let url = viewModel.limeSurveyLink {
                    WebView(url: URLRequest(url: url), viewModel: viewModel.webViewModel)
                        .ignoresSafeArea(.all, edges: .bottom)
                }
            }
        }
        .customNavigationTitle(with: NavigationScreen.limeSurvey.localize(), displayMode: .inline)
        .toolbar {
            if viewModel.wasAnswered {
                Button {
                    viewModel.onFinish()
                } label: {
                    Image(systemName: "checkmark.circle.fill")
                        .foregroundColor(.more.secondary)
                }
            } else {
                Button {
                    viewModel.onFinish()
                } label: {
                    Image(systemName: "chevron.down")
                        .foregroundColor(.more.important)
                }
            }
        }
        .onAppear {
            viewModel.viewDidAppear()
        }
        .onDisappear {
            viewModel.viewDidDisappear()
        }
    }
}

struct LimeSurveyView_Previews: PreviewProvider {
    static var previews: some View {
        LimeSurveyView(navigationState: NavigationState())
    }
}
