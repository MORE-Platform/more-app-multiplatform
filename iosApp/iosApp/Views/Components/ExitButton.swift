//
//  ExitButton.swift
//  BlendedCare
//
//  Created by Jan Cortiel on 27.01.26.
//  Copyright © 2026 Redlink GmbH. All rights reserved.
//  Copyright © 2024 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung
//  Licensed under the Apache 2.0 license
//  (see https://www.apache.org/licenses/LICENSE-2.0).
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
