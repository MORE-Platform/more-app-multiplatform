//
//  StudyDetailsView.swift
//  iosApp
//
//  Created by Daniil Barkov on 22.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import shared
import SwiftUI


struct StudyDetailsView: View {
    @StateObject var viewModel: StudyDetailsViewModel
    private let stringTable = "StudyDetailsView"
    
    @State var selection: Int = 0
    private let navigationStrings = "Study Details"
    
    var body: some View {
        MoreMainBackgroundView {
            VStack(alignment: .leading) {
                
                DetailsTitle(text: viewModel.studyDetailsModel?.studyTitle ?? "")
                    .font(Font.more.subtitle)
                    .padding(.bottom)
                
                TaskProgressView(progressViewTitle: .constant(String
                    .localizedString(forKey: "tasks_completed", inTable: stringTable,
                                     withComment: "string for completed tasks")), totalTasks: Double(viewModel.totalTasks ?? 0), tasksCompleted: Double(viewModel.completedTasks ?? 0))
                .padding(.bottom, 0.2)
                
                HStack(alignment: .center) {
                    BasicText(text: .constant(String
                        .localizedString(forKey: "study_duration", inTable: stringTable,
                                         withComment: "string for study duration")))
                    Spacer()
                    BasicText(text: .constant((viewModel.studyDetailsModel?.start?.int64Value.toDateString(dateFormat: "dd.MM.yyyy") ?? "")+" - "+(viewModel.studyDetailsModel?.end?.int64Value.toDateString(dateFormat: "dd.MM.yyyy") ?? "")))
                }.padding(.bottom)
                
                DetailsTitle(text: "Participant information")
                    .font(Font.more.subtitle)
                    .padding(.bottom)
                
                Divider()
                
                BasicText(text: .constant(viewModel.studyDetailsModel?.participantInformation ?? ""))
                    .padding(.bottom)
                
                DetailsTitle(text: "Observation Modules")
                    .font(Font.more.subtitle)
                    .padding(.bottom)
                
                Divider()
            
                BasicText(text: .constant("hello"))
                
                
//
//                if selection == 0 {
//                    EmptyView()
//                } else {
//                    EmptyView()
//                }
            }
        } topBarContent: {
            EmptyView()
        }
        .customNavigationTitle(with: NavigationScreens.studyDetails.localize(useTable: navigationStrings, withComment: "Study Details title"))
        .navigationBarTitleDisplayMode(.inline)
    }
}

struct StudyDetailsView_Previews: PreviewProvider {
    static var previews: some View {
        StudyDetailsView(viewModel: StudyDetailsViewModel())
    }
}
