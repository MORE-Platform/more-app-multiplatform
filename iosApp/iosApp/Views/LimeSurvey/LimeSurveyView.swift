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
    @EnvironmentObject var questionModalState: QuestionModalState
    
    @Environment(\.presentationMode) var presentationMode
    
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
                            WebView(url: url)
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
                    Button("Done".localize(useTable: stringsTable, withComment: "LimeSurvey done")) {
                        viewModel.onFinish()
                        presentationMode.wrappedValue.dismiss()
                    }
                } else {
                    Button("Cancel".localize(useTable: stringsTable, withComment: "Cancel LimeSurvey")) {
                        viewModel.onFinish()
                        presentationMode.wrappedValue.dismiss()
                    }
                }
            }
        }
    }
}

struct LimeSurveyView_Previews: PreviewProvider {
    static var previews: some View {
        LimeSurveyView(viewModel: LimeSurveyViewModel(scheduleId: ""))
            .environmentObject(QuestionModalState())
    }
}
