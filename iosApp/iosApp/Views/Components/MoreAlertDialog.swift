//
//  MoreAlertDialog.swift
//  More
//
//  Created by Jan Cortiel on 25.01.24.
//  Copyright © 2024 Redlink GmbH. All rights reserved.
//

import shared
import SwiftUI

struct MoreAlertDialog: View {
    var alertDialogModel: AlertDialogModel

    private let stringTable = "AlertDialog"
    var body: some View {
        ZStack {
            Color.black.opacity(0.5)
                .ignoresSafeArea(edges: .all)
            VStack(spacing: 20) {
                Text(String.localize(forKey: alertDialogModel.title, withComment: "alert dialog title", inTable: stringTable))
                    .foregroundColor(.more.primary)
                    .font(.headline)
                    .multilineTextAlignment(.center)
                    .frame(maxWidth: .infinity)
                Divider()
                    .padding(.horizontal)

                ScrollView(.vertical, showsIndicators: false) {
                    VStack(alignment: .leading) {
                        Text(String.localize(forKey: alertDialogModel.message, withComment: "alert dialog message", inTable: stringTable))
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
                        alertDialogModel.onPositive()
                    } label: {
                        Text(String.localize(forKey: alertDialogModel.positiveTitle, withComment: "positive button", inTable: stringTable))
                    }

                    if let negativeTitle = alertDialogModel.negativeTitle {
                        MoreActionButton(backgroundColor: .more.secondaryLight, disabled: .constant(false)) {
                            alertDialogModel.onNegative()
                        } label: {
                            if #available(iOS 17.0, *) {
                                Text(String.localize(forKey: negativeTitle, withComment: "negative button", inTable: stringTable))
                                    .foregroundStyle(Color.more.primary)
                            } else {
                                Text(String.localize(forKey: negativeTitle, withComment: "negative button", inTable: stringTable))
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
    MoreAlertDialog(alertDialogModel: AlertDialogModel(title: "Needed permissions were not given", message: "This study needs one or more sensor permission to correctly work. You may decline sensor permissions, but if you do, the app and the study may not work fully or as expected. Would you like to go to the settings and allow the app to access needed sensor permissions?", positiveTitle: "Required Permissions Were Not Granted", negativeTitle: "Continue without allowing", onPositive: {
        print("Settings")
    }, onNegative: {
        print("Continue")
    }))
}
