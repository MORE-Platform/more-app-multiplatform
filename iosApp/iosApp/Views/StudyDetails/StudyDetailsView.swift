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

    @EnvironmentObject private var navigationModalState: NavigationModalState

    var body: some View {
        ScrollView {
            VStack(alignment: .leading) {
                Title2(titleText: viewModel.studyDetailsModel?.study.studyTitle ?? "")
                    .padding(.top)
                    .padding(.bottom)

                TaskCompletionBarView(viewModel: TaskCompletionBarViewModel(), progressViewTitle: "tasks_completed")
                    .padding(.bottom, 0.2)

                HStack(alignment: .center) {
                    BasicText(text: "study_duration")

                    Spacer()
                    BasicText(text: (viewModel.studyStart.formattedString()) + " - " + (viewModel.studyEnd.formattedString()),
                              color: Color.more.secondary
                    )
                }.padding(.bottom)

                ExpandableText(viewModel.studyDetailsModel?.study.participantInfo ?? "", "participant_info", lineLimit: 4)
                    .padding(.bottom, 35)

                ExpandableContentWithLink(
                    content: {
                        ScrollView {
                            VStack {
                                ForEach(viewModel.studyDetailsModel?.observations ?? [], id: \.self) { obs in
                                    Button {
                                        navigationModalState.openView(screen: .observationDetails, observationId: obs.observationId)
                                    } label: {
                                        ModuleListItem(observation: obs)
                                            .padding(.bottom)
                                    }
                                }
                            }
                        }
                    },
                    title: { String(localized: "obs_modules") }, expanded: $isObservationListOpen
                ).padding(.top, 0.5)

                Spacer()
            }
        }
        .customNavigationTitle(with: NavigationScreen.studyDetails.localize(), displayMode: .inline)
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
