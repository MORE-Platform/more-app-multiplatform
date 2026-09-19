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
//  Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
//

import SwiftUI
import shared

struct SettingsView: View {
    @StateObject private var viewModel: SettingsViewModel = SettingsViewModel()
    @State private var exitButton = Color.more.important

    var body: some View {
        VStack(alignment: .leading) {
            MoreActionButton(
                disabled: .constant(false),
                action: {
                    viewModel.coreViewModel.openSettings()
                }
            ) {
                Text("open_settings")
            }
            .padding(.bottom, 16)

            if viewModel.needsTracking {
                VStack(alignment: .leading) {
                    HStack(alignment: .center) {
                        Toggle(isOn: viewModel.allowTrackingBinding) {
                            Text(
                                SharedRes
                                    .strings()
                                    .app_tracking_dialog_title
                                    .desc()
                                    .localized()
                            )
                        }
                    }
                    Divider()
                    Text(
                        SharedRes
                            .strings()
                            .app_tracking_dialog_message
                            .desc()
                            .localized()
                    )
                }
                .padding(12)
                .overlay(
                    RoundedRectangle(cornerRadius: 12)
                        .stroke(Color.more.secondary, lineWidth: 1)
                )
                .clipShape(RoundedRectangle(cornerRadius: 12))
                .padding(.bottom, 8)

            }

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
        .onAppear {
            viewModel.coreViewModel.viewDidAppear()
        }
        .onDisappear {
            viewModel.coreViewModel.viewDidDisappear()
        }
    }
}

struct SettingsView_Previews: PreviewProvider {
    static var previews: some View {
        SettingsView()
    }
}
