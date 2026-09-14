//
//  GarminConnectView.swift
//  More
//
//  Created by Jan Cortiel on 13.11.25.
//  Copyright © 2025 Redlink GmbH. All rights reserved.
//  Copyright © 2024 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung
//  Licensed under the Apache 2.0 license
//  (see https://www.apache.org/licenses/LICENSE-2.0).
//

import SwiftUI

struct GarminConnectView: View {
    @Environment(\.dismiss) private var dismiss
    @StateObject private var viewModel = GarminConnectViewModel()

    var body: some View {
        MoreMainBackgroundView(contentPadding: 0) {
            VStack {
                if let url = viewModel.getUrl() {
                    WebView(url: url, viewModel: viewModel.webViewModel)
                        .ignoresSafeArea(.all, edges: .bottom)
                } else {
                    Text("Could not receive the url")
                }
            }
        }
        .customNavigationTitle(with: NavigationScreen.garminConnect.localize(), displayMode: .inline)
        .toolbar {
            Button {
                viewModel.coreViewModel.closeView()
            } label: {
                Image(systemName: "chevron.down")
                    .foregroundColor(.more.important)
            }
        }
        .onAppear {
            viewModel.coreViewModel.viewDidAppear()
            viewModel.clearAllWebViewData()
        }
        .onDisappear() {
            viewModel.coreViewModel.viewDidDisappear()
        }
        .onReceive(viewModel.$shouldClose.removeDuplicates()) { close in
            if close {
                dismiss()
            }
        }
    }
}

#Preview {
    GarminConnectView()
}
