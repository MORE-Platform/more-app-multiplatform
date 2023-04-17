//
//  ExitStudyLevelOneView.swift
//  iosApp
//
//  Created by Daniil Barkov on 13.04.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import SwiftUI

struct ExitStudyLevelOneView: View {
    @StateObject var viewModel: SettingsViewModel
    
    private let stringTable = "SettingsView"
    private let navigationStrings = "Navigation"
    
    var body: some View {
        MoreMainBackgroundView {
            VStack(alignment: .leading) {
                Title2(titleText: .constant(viewModel.study?.studyTitle ?? ""))
                    .padding(.top)
                    .padding(.bottom)
                HStack(alignment: .center) {
                    Spacer()
                    
                    Image(systemName: "exclamationmark.triangle.fill")
                        .font(.system(size: 48))
                        .foregroundColor(Color.more.important)
                        .padding()
                    Spacer()
                
                }.padding(.top)
                Text(String.localizedString(forKey: "first_message", inTable: stringTable, withComment: "exit message"))
                    .foregroundColor(Color.more.important)
                    .fontWeight(.bold)
                    .padding(.bottom, 2)
                Spacer()
                
                HStack{
                    Spacer()
                    Text(String.localizedString(forKey: "really_message", inTable: stringTable, withComment: "second question message"))
                        .padding(.bottom)
                    Spacer()
                }
                NavigationLink {
                    InfoView()
                } label: {
                    MoreActionButton(backgroundColor: Color.more.approved, disabled: .constant(false)) {
                    } label: {
                        Text(String.localizedString(forKey: "continue_study", inTable: stringTable, withComment: "button to continue study"))
                    }
                }
                NavigationLink {
                    ExitStudyLevelTwoView(viewModel: viewModel)
                } label: {
                    MoreActionButton(backgroundColor: Color.more.important, disabled: .constant(false)) {
                    } label: {
                        Text(String.localizedString(forKey: "withdraw_study", inTable: stringTable, withComment: "button to withdraw study"))
                    }
                }
                
                Spacer()
            }
            
            
            
            
        } topBarContent: {
            EmptyView()
        }
        .customNavigationTitle(with: NavigationScreens.settings.localize(useTable: navigationStrings, withComment: "Settings Screen"))
        .navigationBarTitleDisplayMode(.inline)
    }
}

//struct ExitStudyLevelOneView_Previews: PreviewProvider {
//    static var previews: some View {
//        ExitStudyLevelOneView()
//    }
//}
