//
//  ExitStudyLevelOneView.swift
//  iosApp
//
//  Created by Daniil Barkov on 13.04.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
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

                    Title2(titleText: .constant(viewModel.study?.studyTitle ?? ""))
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

                    SectionHeading(sectionTitle: .constant(String.localizedString(forKey: "first_message", inTable: stringTable, withComment: "exit message")))
                        .foregroundColor(Color.more.important)
                        .padding(.bottom, 2)
                        .multilineTextAlignment(.center)

                    Spacer()

                    HStack{
                        Spacer()
                        Text(String.localizedString(forKey: "really_message", inTable: stringTable, withComment: "second question message"))
                            .padding(.bottom)
                        Spacer()
                    }

                    MoreActionButton(
                        backgroundColor: .more.approved,
                        disabled: .constant(false)
                    ) {
                        contentViewModel.isLeaveStudyOpen = false
                    } label: {
                        Text(String.localizedString(forKey: "continue_study", inTable: stringTable, withComment: "button to continue study")).foregroundColor(Color.more.white)
                    }
                    .padding(.bottom, 2)

                    MoreActionButton(
                        backgroundColor: .more.important,
                        disabled: .constant(false)
                    ) {
                        contentViewModel.isLeaveStudyConfirmOpen = true
                    } label: {
                        Text(String.localizedString(forKey: "withdraw_study", inTable: stringTable, withComment: "button to withdraw study")).foregroundColor(Color.more.white)
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
