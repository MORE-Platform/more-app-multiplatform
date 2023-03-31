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
            ScrollView{
                VStack(alignment: .leading) {
                    
                    Title2(titleText: .constant(viewModel.studyDetailsModel?.studyTitle ?? ""))
                        .padding(.top)
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
                        BasicText(text: .constant((viewModel.studyDetailsModel?.start?.int64Value.toDateString(dateFormat: "dd.MM.yyyy") ?? "")+" - "+(viewModel.studyDetailsModel?.end?.int64Value.toDateString(dateFormat: "dd.MM.yyyy") ?? "")),
                                  color: Color.more.secondary
                        )
                    }.padding(.bottom)
                    
                    
                    
                    ExpandableText(viewModel.permissionModel.studyParticipantInfo, String.localizedString(forKey: "participant_info", inTable: stringTable, withComment: "Participant Information of study."), lineLimit: 4)
                        .padding(.bottom, 35)
                    
                    
                    
                    
                    Collapsible(label: {BasicText(text: .constant(String
                        .localizedString(forKey: "obs_modules", inTable: stringTable,
                                         withComment: "string for observation modules")), font: .more.headline)},
                                content: {
                        
                        VStack {
                            ForEach(viewModel.studyDetailsModel?.observations ?? [ObservationSchema()], id:\.self) {
                                obs in
                                ModuleListItem(observation: obs).padding(.bottom)
                            }
                        }.padding()
                    }).padding(.top, (0.5))
                    
                    Spacer()
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
