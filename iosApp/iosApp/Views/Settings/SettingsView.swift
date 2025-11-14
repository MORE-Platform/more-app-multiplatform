//
//  SettingsView.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 08.03.23.
//  Copyright © 2023 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung
//  Licensed under the Apache 2.0 license with Commons Clause
//  (see https://www.apache.org/licenses/LICENSE-2.0 and
//  https://commonsclause.com/).
//

import shared
import SwiftUI

struct SettingsView: View {
    @StateObject private var viewModel: SettingsViewModel = SettingsViewModel()
    @State private var exitButton = Color.more.important

    private let stringTable = "SettingsView"
    private let navigationStrings = "Navigation"

    var body: some View {
        VStack(alignment: .leading) {
            Text("settings_text")
                .foregroundColor(.more.secondary)
                .padding(.bottom, 15)
            if let permissions = viewModel.permissionModel {
                ConsentList(permissionModel: permissions)
                    .padding(.top)
            }

            Spacer()
        }
        .customNavigationTitle(with: NavigationScreen.settings.localize())
    }
}

struct SettingsView_Previews: PreviewProvider {
    static var previews: some View {
        SettingsView()
    }
}
