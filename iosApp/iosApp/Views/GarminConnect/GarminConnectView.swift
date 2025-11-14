//
//  GarminConnectView.swift
//  More
//
//  Created by Jan Cortiel on 13.11.25.
//  Copyright © 2025 Redlink GmbH. All rights reserved.
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
