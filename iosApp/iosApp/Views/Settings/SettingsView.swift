//
//  SettingsView.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 08.03.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import SwiftUI
import shared

struct SettingsView: View {
    @StateObject var viewModel: SettingsViewModel
    @State var exitButton = Color.more.important
    
    private let stringTable = "SettingsView"
    private let navigationStrings = "Navigation"
    
    var body: some View {
        MoreMainBackgroundView {
            VStack(alignment: .leading) {
                Text(String.localize(forKey: "settings_text", withComment: "information about accepted permissions", inTable: stringTable))
                    .foregroundColor(.more.secondary)
                    .padding(.bottom, 15)
                if let permissions = viewModel.permissionModel {
                    ConsentList(permissionModel: permissions)
                        .padding(.top)
                }
                
                Spacer()
            }
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

struct SettingsView_Previews: PreviewProvider {
    static var previews: some View {
        SettingsView(viewModel: SettingsViewModel())
    }
}
