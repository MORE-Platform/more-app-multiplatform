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
                
                Text(String.localizedString(forKey: "second_message", inTable: stringTable, withComment: "second exit message"))
                    .foregroundColor(Color.more.secondary)
                    .padding(.bottom, 2)
                
                Text(String.localizedString(forKey: "sure_message", inTable: stringTable, withComment: "last exit message"))
                    .foregroundColor(Color.more.secondary)
                    .fontWeight(.bold)
                    .padding(.bottom, 2)
                
                Spacer()
                
                NavigationLink {
                    InfoView()
                } label: {
                    MoreActionButton(backgroundColor: Color.more.approved, disabled: .constant(false)) {
                    } label: {
                        Text(String.localizedString(forKey: "continue_study", inTable: stringTable, withComment: "button to continue study configuration"))
                    }.padding(.bottom)
                }.padding(.bottom)
                
                
                TriggerSlider(sliderView: {
                    RoundedRectangle(cornerRadius: .moreBorder.cornerRadius, style: .continuous).fill(Color.more.important)
                        .overlay(Image(systemName: "arrow.right").font(.system(size: 30)).foregroundColor(.white))
                }, textView: {
                    Text(String.localizedString(forKey: "withdraw_swipe", inTable: stringTable, withComment: "button to refresh study configuration")).foregroundColor(Color.more.important)
                },
                              backgroundView: {
                    RoundedRectangle(cornerRadius: .moreBorder.cornerRadius, style: .continuous)
                        .fill(Color.more.importantLight)
                }, offsetX: self.$simpleRightDirectionSliderOffsetX,
                              didSlideToEnd: {
                    self.alertPresented = true
                    viewModel.leaveStudy()
                }, settings: TriggerSliderSettings(sliderViewVPadding: 5, slideDirection: .right))
                
                
            } // VStack
            
        } // MoreBackground
    topBarContent: {
        EmptyView()
    }
    .customNavigationTitle(with: NavigationScreens.settings.localize(useTable: navigationStrings, withComment: "Settings Screen"))
    .navigationBarTitleDisplayMode(.inline)
    } // View
}
//
//struct ExitStudyLevelTwoView_Previews: PreviewProvider {
//    static var previews: some View {
//        ExitStudyLevelTwoView()
//    }
//}
