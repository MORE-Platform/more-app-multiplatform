//
//  ExitStudyLevelTwoView.swift
//  iosApp
//
//  Created by Daniil Barkov on 13.04.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import SwiftUI
struct LeaveStudyConfirmationView: View {
    @StateObject var viewModel: SettingsViewModel
    @EnvironmentObject var contentViewModel: ContentViewModel
    var float = CGFloat(0)
    
    private let stringTable = "SettingsView"
    private let navigationStrings = "Navigation"
    
    @State private var simpleRightDirectionSliderOffsetX: CGFloat = 0
    @State private var simpleLeftDirectionSliderOffsetX: CGFloat = 0
    @State private var rectangularSliderOffsetX: CGFloat = 0
    @State private var  neumorphicSliderOffsetX: CGFloat = 0
    @State private var alertPresented: Bool = false
    @State var continueButton = Color.more.approved
    
    
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

                    Text(String.localize(forKey: "second_message", withComment: "second exit message", inTable: stringTable))
                        .foregroundColor(Color.more.secondary)
                        .padding(.bottom, 2)
                        .multilineTextAlignment(.center)

                    Text(String.localize(forKey: "sure_message", withComment: "last exit message", inTable: stringTable))
                        .foregroundColor(Color.more.primary)
                        .fontWeight(.bold)
                        .padding(.bottom, 2)
                        .multilineTextAlignment(.center)

                    Spacer()
                        .frame(height: 150)

                    MoreActionButton(
                        backgroundColor: .more.approved,
                        disabled: .constant(false)
                    ) {
                        contentViewModel.isLeaveStudyConfirmOpen = false
                        contentViewModel.isLeaveStudyOpen = false
                    } label: {
                        Text(String.localize(forKey: "continue_study", withComment: "button to continue study", inTable: stringTable)).foregroundColor(Color.more.white)
                    }
                    .padding(.bottom, 2)

                    MoreActionButton(backgroundColor: .more.important, disabled: .constant(false)) {
                        viewModel.leaveStudy()
                        contentViewModel.isLeaveStudyConfirmOpen = false
                        contentViewModel.isLeaveStudyConfirmOpen = false
                    } label: {
                        Text(String.localize(forKey: "withdraw", withComment: "button to exit study", inTable: stringTable))
                    }

                    Spacer()
                }
                .padding(.horizontal, 40)
                
            }
            .customNavigationTitle(with: NavigationScreens.settings.localize(useTable: navigationStrings, withComment: "Settings Screen"))
            .onAppear {
                viewModel.viewDidAppear()
            }
            .onDisappear{
                viewModel.viewDidDisappear()
            }
            
        }
    
    }
}
