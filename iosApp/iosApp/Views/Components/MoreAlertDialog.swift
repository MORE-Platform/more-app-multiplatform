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
    let alertDialogModel: AlertDialogModel
    
    var body: some View {
        ZStack {
            Color.black.opacity(0.5)
                .ignoresSafeArea(edges: .all)
            VStack(spacing: 20) {
                Text(LocalizedStringKey(alertDialogModel.title))
                    .foregroundColor(.more.primary)
                    .font(.headline)
                    .multilineTextAlignment(.center)
                    .frame(maxWidth: .infinity)
                Divider()
                    .padding(.horizontal)

                ScrollView(.vertical, showsIndicators: false) {
                    VStack(alignment: .leading) {
                        Text(LocalizedStringKey(alertDialogModel.message))
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
                        if let onPositive = alertDialogModel.onPositive {
                            onPositive()
                        }
                    } label: {
                        Text(LocalizedStringKey(alertDialogModel.positiveTitle))
                    }

                    if let negativeTitle = alertDialogModel.negativeTitle {
                        MoreActionButton(backgroundColor: .more.secondaryLight, disabled: .constant(false)) {
                            if let onNegative = alertDialogModel.onNegative {
                                onNegative()
                            }
                        } label: {
                            if #available(iOS 17.0, *) {
                                Text(LocalizedStringKey(negativeTitle))
                                    .foregroundStyle(Color.more.primary)
                            } else {
                                Text(LocalizedStringKey(negativeTitle))
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
