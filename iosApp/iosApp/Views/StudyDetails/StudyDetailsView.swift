//
//  StudyDetailsView.swift
//  iosApp
//
//  Created by Daniil Barkov on 22.03.23.
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


struct StudyDetailsView: View {
    @StateObject var viewModel: StudyDetailsViewModel
    private let stringTable = "StudyDetailsView"
    
    @State var selection: Int = 0
    private let navigationStrings = "Navigation"
    
    @State private var isObservationListOpen = false
    
    var body: some View {
        MoreMainBackgroundView {
            ScrollView {
                VStack(alignment: .leading) {
                    Title2(titleText: viewModel.studyDetailsModel?.study.studyTitle ?? "")
                        .padding(.top)
                        .padding(.bottom)
                    
                    TaskCompletionBarView(viewModel: TaskCompletionBarViewModel(), progressViewTitle: String.localize(forKey: "tasks_completed", withComment: "string for completed tasks", inTable: stringTable))
                    .padding(.bottom, 0.2)
                    
                    HStack(alignment: .center) {
                        
                        BasicText(text: String
                            .localize(forKey: "study_duration", withComment: "string for study duration", inTable: stringTable))
                        
                        Spacer()
                        BasicText(text: (viewModel.studyStart.formattedString()) + " - " + (viewModel.studyEnd.formattedString()),
                                  color: Color.more.secondary
                        )
                    }.padding(.bottom)
                    
                    ExpandableText(viewModel.studyDetailsModel?.study.participantInfo ?? "", String.localize(forKey: "participant_info", withComment: "Participant Information of study.", inTable: stringTable), lineLimit: 4)
                        .padding(.bottom, 35)
                    
                    ExpandableContentWithLink(
                        content: {
                            ScrollView {
                                VStack {
                                    ForEach(viewModel.studyDetailsModel?.observations ?? [ObservationSchema()], id:\.self) { obs in
                                        NavigationLink {
                                            ObservationDetailsView(viewModel: ObservationDetailsViewModel(observationId: obs.observationId))
                                        } label: {
                                            ModuleListItem(observation: obs).padding(.bottom)
                                        }
                                    }
                                }
                            }
                        },
                        title: {String.localize(forKey: "obs_modules", withComment: "Observation modules of study.", inTable: stringTable)}, expanded: $isObservationListOpen
                    ).padding(.top, (0.5))
                    
                    Spacer()
                }
                
            }
        }
        .customNavigationTitle(with: NavigationScreens.studyDetails.localize(useTable: navigationStrings, withComment: "Study Details"), displayMode: .inline)
        .onAppear {
            viewModel.viewDidAppear()
        }
        .onDisappear {
            viewModel.viewDidDisappear()
        }
    }
}

struct StudyDetailsView_Previews: PreviewProvider {
    static var previews: some View {
        StudyDetailsView(viewModel: StudyDetailsViewModel())
    }
}

extension Binding where Value == Bool {
    var not: Binding<Value> {
        Binding<Value>(
            get: { !self.wrappedValue },
            set: { self.wrappedValue = !$0 }
        )
    }
}


