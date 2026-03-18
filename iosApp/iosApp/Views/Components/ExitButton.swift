//
//  ExitButton.swift
//  BlendedCare
//
//  Created by Jan Cortiel on 27.01.26.
//  Copyright © 2026 Redlink GmbH. All rights reserved.
//

import SwiftUI
import shared

struct ExitButton: View {
    @EnvironmentObject private var navigationModalState: NavigationModalState
    var body: some View {
        MoreActionButton(backgroundColor: .more.important, disabled: .constant(false)) {
            withdraw()
        } label: {
            Text("withdraw")
        }
    }
    
    private func withdraw() {
        AlertController.shared.openAlertDialog(model: AlertDialogModel.companion.fromStrings(
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
    ExitButton()
}
