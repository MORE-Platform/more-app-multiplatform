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
    @State var totalTasks: Double = 0
    @State var selection: Int = 0
    @State var tasksCompleted: Double = 0
    private let navigationStrings = "Study Details"
    
    var body: some View {
        MoreMainBackgroundView {
            VStack(alignment: .leading) {
                DetailsTitle(text: $viewModel.studyTitle)
                    .font(Font.more.subtitle)
                    .padding(.bottom)
                TaskProgressView(progressViewTitle: .constant(String
                    .localizedString(forKey: "tasks_completed", inTable: stringTable,
                                     withComment: "string for completed tasks")), totalTasks: totalTasks, tasksCompleted: tasksCompleted)
                .padding(.bottom)
                if selection == 0 {
                    EmptyView()
                } else {
                    EmptyView()
                }
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
