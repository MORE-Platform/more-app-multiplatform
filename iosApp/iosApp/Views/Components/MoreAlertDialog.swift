//
//  MoreAlertDialog.swift
//  More
//
//  Created by Jan Cortiel on 25.01.24.
//  Copyright © 2024 Redlink GmbH. All rights reserved.
//

import SwiftUI
import shared

struct MoreAlertDialog: View {
    let alertDialogModel: AlertDialogModel

    var body: some View {
        ZStack {
            Color.black.opacity(0.5)
                .ignoresSafeArea(edges: .all)
            VStack(spacing: 20) {
                Text(alertDialogModel.title.localized())
                    .foregroundColor(.more.primary)
                    .font(.headline)
                    .multilineTextAlignment(.center)
                    .frame(maxWidth: .infinity)
                Divider()
                    .padding(.horizontal)

                ScrollView(.vertical, showsIndicators: false) {
                    VStack(alignment: .leading) {
                        Text(alertDialogModel.message.localized())
                            .foregroundColor(.more.primary)
                            .font(.subheadline)
                            .multilineTextAlignment(.leading)
                            .frame(maxWidth: .infinity, alignment: .leading)
                    }
                }
                .frame(minHeight: 0, maxHeight: 275)
                .padding(.horizontal)

                VStack {
                    MoreActionButton(disabled: .constant(false)) {
                        if let onConfirm = alertDialogModel.onConfirm {
                            onConfirm()
                        }
                    } label: {
                        Text(alertDialogModel.confirmLabel.localized())
                    }

                    if let cancelLabel = alertDialogModel.cancelLabel {
                        MoreActionButton(backgroundColor: .more.secondaryLight, disabled: .constant(false)) {
                            if let onDecline = alertDialogModel.onDecline {
                                onDecline()
                            }
                        } label: {
                            if #available(iOS 17.0, *) {
                                Text(cancelLabel.localized())
                                    .foregroundStyle(Color.more.primary)
                            } else {
                                Text(cancelLabel.localized())
                                    .foregroundColor(.more.primary)
                            }
                        }
                    }
                }
                .padding(.horizontal)
            }
            .padding(.vertical)
            .padding(.horizontal, 4)
            .background(Color.white)
            .cornerRadius(10)
            .shadow(radius: 5)
            .frame(maxWidth: .infinity)
            .padding()
        }
    }
}

#Preview {
    MoreAlertDialog(
        alertDialogModel: AlertDialogModel.companion.fromStrings(
            title: "Needed permissions were not given",
            message: "This study needs one or more sensor permission to correctly work. You may decline sensor permissions, but if you do, the app and the study may not work fully or as expected. Would you like to go to the settings and allow the app to access needed sensor permissions?",
            confirmLabel: "Required Permissions Were Not Granted",
            cancelLabel: "Continue without allowing",
            onConfirm: {
                print("Settings")
            },
            onDecline: {
                print("Continue")
            }))
}
