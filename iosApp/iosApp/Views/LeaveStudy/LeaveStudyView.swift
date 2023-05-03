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
    
    private let stringTable = "SettingsView"
    private let navigationStrings = "Navigation"
    @State var accButton = Color.more.approved
    @State var decButton = Color.more.important
    
    var body: some View {
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
                        .font(.system(size: 48))
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
                
                BasicNavLinkButton(backgroundColor: $accButton){
                    InfoView(viewModel: InfoViewModel())
                } label: {
                    Text(String.localizedString(forKey: "continue_study", inTable: stringTable, withComment: "button to continue study")).foregroundColor(Color.more.white)
                }
                
                BasicNavLinkButton(backgroundColor: $decButton){
                    LeaveStudyConfirmationView(viewModel: viewModel)
                } label: {
                    Text(String.localizedString(forKey: "withdraw_study", inTable: stringTable, withComment: "button to withdraw study")).foregroundColor(Color.more.white)
                }
                
                Spacer()
            }
            
            
            
            
        } topBarContent: {
            EmptyView()
        }
        .customNavigationTitle(with: NavigationScreens.settings.localize(useTable: navigationStrings, withComment: "Settings Screen"))
        .navigationBarTitleDisplayMode(.inline)
        .onAppear {
            viewModel.viewDidAppear()
        }
        .onDisappear{
            viewModel.viewDidDisappear()
        }
    }
}
