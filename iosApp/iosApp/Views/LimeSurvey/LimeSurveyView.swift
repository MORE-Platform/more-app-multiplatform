//
//  LimeSurveyView.swift
//  More
//
//  Created by Jan Cortiel on 11.05.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import SwiftUI

struct LimeSurveyView: View {
    @StateObject var viewModel: LimeSurveyViewModel
    
    private let stringsTable = "LimeSurvey"
    var body: some View {
        Navigation {
            MoreMainBackgroundView(contentPadding: 0) {
                VStack {
                    if viewModel.dataLoading {
                        HStack {
                            Text("Data is loading...")
                        }
                    } else {
                        if let url = viewModel.limeSurveyLink {
                            WebView(url: url, viewModel: viewModel.webViewModel)
                                .ignoresSafeArea(.all, edges: .bottom)
                        } else {
                            Text("URL is nil")
                        }
                    }
                }
            }
            .customNavigationTitle(with: NavigationScreens.limeSurvey.localize(useTable: stringsTable, withComment: "LimeSurvey View"), displayMode: .inline)
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
                        Image(systemName: "trash.circle.fill")
                            .foregroundColor(.more.important)
                    }
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
        LimeSurveyView(viewModel: LimeSurveyViewModel(navigationModalState: NavigationModalState()))
    }
}
