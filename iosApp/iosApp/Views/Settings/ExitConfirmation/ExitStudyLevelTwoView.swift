//
//  ExitStudyLevelTwoView.swift
//  iosApp
//
//  Created by Daniil Barkov on 13.04.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import SwiftUI
struct ExitStudyLevelTwoView: View {
    @StateObject var viewModel: SettingsViewModel
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
            VStack(alignment: .leading) {
                
                Title2(titleText: .constant(viewModel.study?.studyTitle ?? ""))
                    .padding(.top)
                    .padding(.bottom)
                
                Spacer()
                
                HStack(alignment: .center) {
                    Spacer()
                    
                    Image(systemName: "exclamationmark.triangle.fill")
                        .font(.system(size: 48))
                        .foregroundColor(Color.more.important)
                        .padding()
                    Spacer()
                    
                }.padding(.top)
                
                Text(String.localizedString(forKey: "second_message", inTable: stringTable, withComment: "second exit message"))
                    .foregroundColor(Color.more.secondary)
                    .padding(.bottom, 2)
                
                Text(String.localizedString(forKey: "sure_message", inTable: stringTable, withComment: "last exit message"))
                    .foregroundColor(Color.more.secondary)
                    .fontWeight(.bold)
                    .padding(.bottom, 2)
                
                Spacer()
                    .frame(height: 150)
                
                BasicNavLinkButton(backgroundColor: $continueButton) {
                    InfoView()
                } label: {
                    Text(String.localizedString(forKey: "back_to_settings", inTable: stringTable, withComment: "button to continue taking part in the study")).foregroundColor(Color.more.white)
                }.padding(.bottom)
                
                TriggerSlider(
                    sliderView: {
                        RoundedRectangle(cornerRadius: .moreBorder.cornerRadius, style: .continuous).fill(Color.more.important)
                            .overlay(Image(systemName: "arrow.right").font(.system(size: 30)).foregroundColor(.white))
                    }, textView: {
                        Text(String.localizedString(forKey: "withdraw_swipe", inTable: stringTable, withComment: "button to refresh study configuration")).foregroundColor(Color.more.important)
                    }, backgroundView: {
                        RoundedRectangle(cornerRadius: .moreBorder.cornerRadius, style: .continuous)
                            .fill(Color.more.importantLight)
                    }, offsetX: self.$simpleRightDirectionSliderOffsetX,
                    didSlideToEnd: {
                        self.alertPresented = true
                        viewModel.leaveStudy()
                    }, settings: TriggerSliderSettings()).foregroundColor(Color.more.white)
                
                
            } // VStack
            
        } // MoreBackground
    topBarContent: {
        EmptyView()
    }
    .customNavigationTitle(with: NavigationScreens.settings.localize(useTable: navigationStrings, withComment: "Settings Screen"))
    .navigationBarTitleDisplayMode(.inline)
    } // View
}
