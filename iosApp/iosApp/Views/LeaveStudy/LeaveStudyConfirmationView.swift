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
    @EnvironmentObject var leaveStudyModalStateVM: LeaveStudyModalStateViewModel
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
            MoreMainBackground {
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
                    
                    Text(String.localizedString(forKey: "second_message", inTable: stringTable, withComment: "second exit message"))
                        .foregroundColor(Color.more.secondary)
                        .padding(.bottom, 2)
                        .multilineTextAlignment(.center)
                    
                    Text(String.localizedString(forKey: "sure_message", inTable: stringTable, withComment: "last exit message"))
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
                        leaveStudyModalStateVM.isLeaveStudyOpen = false
                        leaveStudyModalStateVM.isLeaveStudyConfirmOpen = false
                    } label: {
                        Text(String.localizedString(forKey: "continue_study", inTable: stringTable, withComment: "button to continue study")).foregroundColor(Color.more.white)
                    }
                    .padding(.bottom, 2)
                    
                    MoreActionButton(backgroundColor: .more.important, disabled: .constant(false)) {
                        leaveStudyModalStateVM.isLeaveStudyOpen = false
                        leaveStudyModalStateVM.isLeaveStudyConfirmOpen = false
                        viewModel.leaveStudy()
                    } label: {
                        Text(String.localizedString(forKey: "withdraw", inTable: stringTable, withComment: "button to exit study"))
                    }
                    
                    Spacer()
                }
                .padding(.horizontal, 40)
                
            }
        topBarContent: {
            EmptyView()
        }
        .customNavigationTitle(with: NavigationScreens.withdrawStudyConfirm.localize(useTable: navigationStrings, withComment: "Confirm to withdraw from study"))
        .navigationBarTitleDisplayMode(.inline)
        }
    }
}
