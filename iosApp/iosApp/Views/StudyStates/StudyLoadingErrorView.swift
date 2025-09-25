//
//  StudyLoadingErrorView.swift
//  More
//
//  Created by Jan Cortiel on 25.09.25.
//  Copyright © 2025 Redlink GmbH. All rights reserved.
//

import shared
import SwiftUI

struct StudyLoadingErrorView: View {
    @EnvironmentObject private var navigationModalState: NavigationModalState
    var body: some View {
        VStack(alignment: .center) {
            Spacer()
            Image(systemName: "exclamationmark.triangle.fill")
                .font(.system(size: 60))
                .foregroundColor(Color.more.important)
                .padding()
            Title(titleText: "sure_message", textAlignment: .center)
                .padding(.bottom, 8)
            Title2(titleText: "leave_confirmation_message", textAlignment: .center)
            Spacer()

            MoreActionButton(backgroundColor: .more.important, disabled: .constant(false)) {
                withdraw()
            } label: {
                Text("withdraw")
            }
        }
    }

    private func withdraw() {
        AlertController.shared.openAlertDialog(model: AlertDialogModel(
            title: "sure_message",
            message: "leave_confirmation_message",
            confirmLabel: "withdraw",
            cancelLabel: "continue_study",
            onConfirm: {
                AppDelegate.shared.exitStudy {
                    Task { @MainActor in
                        navigationModalState.clearViews()
                    }
                }
            }))
    }
}

#Preview {
    StudyLoadingErrorView()
}
