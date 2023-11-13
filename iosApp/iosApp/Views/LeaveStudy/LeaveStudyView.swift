//
//  ExitStudyLevelOneView.swift
//  iosApp
//
//  Created by Daniil Barkov on 13.04.23.
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

struct LeaveStudyView: View {
    @StateObject var viewModel: SettingsViewModel
    @EnvironmentObject var contentViewModel: ContentViewModel

    private let stringTable = "SettingsView"
    private let navigationStrings = "Navigation"
    @State var accButton = Color.more.approved
    @State var decButton = Color.more.important
    
    var body: some View {
        Navigation {
            MoreMainBackgroundView {
                VStack(alignment: .leading) {
                    Title2(titleText: viewModel.studyTitle ?? "")
                        .padding(.top)
                        .padding(.bottom)
                        .multilineTextAlignment(.center)
                        .frame(maxWidth: .infinity)

                    Spacer()

                    HStack(alignment: .center) {
                        Spacer()
                        Image(systemName: "exclamationmark.triangle.fill")
                            .font(.system(size: 60))
                            .foregroundColor(Color.more.important)
                            .padding()
                        Spacer()
                    }.padding(.top)

                    SectionHeading(sectionTitle: String.localize(forKey: "first_message", withComment: "exit message", inTable: stringTable))
                        .foregroundColor(Color.more.important)
                        .padding(.bottom, 2)
                        .multilineTextAlignment(.center)

                    Spacer()

                    HStack{
                        Spacer()
                        Text(String.localize(forKey: "really_message", withComment: "second question message", inTable: stringTable))
                            .padding(.bottom)
                        Spacer()
                    }

                    MoreActionButton(
                        backgroundColor: .more.approved,
                        disabled: .constant(false)
                    ) {
                        contentViewModel.isLeaveStudyOpen = false
                    } label: {
                        Text(String.localize(forKey: "continue_study", withComment: "button to continue study", inTable: stringTable)).foregroundColor(Color.more.white)
                    }
                    .padding(.bottom, 2)

                    MoreActionButton(
                        backgroundColor: .more.important,
                        disabled: .constant(false)
                    ) {
                        contentViewModel.isLeaveStudyConfirmOpen = true
                    } label: {
                        Text(String.localize(forKey: "withdraw_study", withComment: "button to withdraw study", inTable: stringTable)).foregroundColor(Color.more.white)
                    }
                    .fullScreenCover(isPresented: $contentViewModel.isLeaveStudyConfirmOpen) {
                        LeaveStudyConfirmationView(viewModel: viewModel)
                            .environmentObject(contentViewModel)
                    }

                    Spacer()
                }
                .padding(.horizontal, 40)

            } 
            .customNavigationTitle(with: NavigationScreens.withdrawStudy.localize(useTable: navigationStrings, withComment: "Withdraw from Study"))
            .onAppear {
                viewModel.viewDidAppear()
            }
            .onDisappear{
                viewModel.viewDidDisappear()
            }
        }
    }
}
