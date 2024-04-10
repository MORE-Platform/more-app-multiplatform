//
//  ExitStudyLevelTwoView.swift
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
struct LeaveStudyConfirmationView: View {
    @StateObject var viewModel: SettingsViewModel
    @EnvironmentObject private var contentViewModel: ContentViewModel
    @EnvironmentObject private var navigationModalState: NavigationModalState
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
        MoreMainBackgroundView {
            VStack {
                Title2(titleText: viewModel.studyTitle ?? "")
                    .padding(.vertical)
                    .multilineTextAlignment(.center)
                    .frame(maxWidth: .infinity)
                
                Spacer()
                
                Image(systemName: "exclamationmark.triangle.fill")
                    .font(.system(size: 60))
                    .foregroundColor(Color.more.important)
                    .padding()
                
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
                    navigationModalState.closeView(screen: .withdrawStudy)
                    navigationModalState.closeView(screen: .withdrawStudyConfirm)
                } label: {
                    Text(String.localize(forKey: "continue_study", withComment: "button to continue study", inTable: stringTable)).foregroundColor(Color.more.white)
                }
                .padding(.bottom, 2)
                
                MoreActionButton(backgroundColor: .more.important, disabled: .constant(false)) {
                    viewModel.leaveStudy()
                    navigationModalState.clearViews()
                } label: {
                    Text(String.localize(forKey: "withdraw", withComment: "button to exit study", inTable: stringTable))
                }
                
                Spacer()
            }
            .padding(.horizontal, 40)
        }
        .customNavigationTitle(with: NavigationScreen.settings.localize(useTable: navigationStrings, withComment: "Settings Screen"))
        .onAppear {
            viewModel.viewDidAppear()
        }
        .onDisappear{
            viewModel.viewDidDisappear()
        }
    
    }
}
