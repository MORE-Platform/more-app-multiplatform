//
//  LoginView.swift
//  iosApp
//
//  Created by Jan Cortiel on 01.02.23.
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

struct LoginView: View {
    @ObservedObject private var registration: RegistrationObservable
    @StateObject private var model: LoginViewModel
    @State private var rotationAngle = 0.0

    @State private var showTokenInput = true
    @State private var showEndpoint = false
    @State private var disabledQRCodeButton = false

    init(registration: RegistrationObservable) {
        _registration = ObservedObject(wrappedValue: registration)
        _model = StateObject(wrappedValue: LoginViewModel(registration: registration.service))
    }

    var body: some View {
        ZStack {
            Color.more.mainBackground
                .ignoresSafeArea()
                .onTapGesture {
                    UIApplication.shared.sendAction(#selector(UIResponder.resignFirstResponder), to: nil, from: nil, for: nil)
                }

            VStack(alignment: .center) {
                Image("more_welcome")
                    .padding(.vertical, 40)

                MoreTextFieldHL(isSmTextfield: .constant(false),
                                headerText: "participation_key_entry",
                                inputPlaceholder: .constant("participation_key_entry"),
                                input: $model.token,
                                capitalization: .uppercase,
                                autoCorrectDisabled: true,
                                textType: .oneTimeCode,
                                hlAlignment: .center
                )
                .padding(.bottom, 12)

                MoreActionButton(backgroundColor: .more.secondary, disabled: $disabledQRCodeButton) {
                    model.showQRCodeView = true
                } label: {
                    HStack {
                        Text("scan_qr_code")
                        Spacer()
                        Image(systemName: "qrcode")
                            .foregroundColor(.more.primaryLight200)
                    }
                }
                .sheet(isPresented: $model.showQRCodeView) {
                    ScanQRCodeView(model: model)
                }
                .padding(.bottom, 12)

                Divider()

                if showTokenInput {
                    VStack {
                        if let error = registration.error {
                            let errorMessage = if error.code == 404 {
                                "Token or Endpoint invalid"
                            } else if let code = error.code?.intValue, code >= 500 && code < 600 {
                                "System Error! Please try again later or contact your Study Administrator!"
                            } else if error.message.count > 0 {
                                error.message
                            } else {
                                "token_error"
                            }
                            ErrorText(message: errorMessage)
                                .padding(.bottom, 5)
                        }

                        VStack(alignment: .center) {
                            if registration.isLoading {
                                ProgressView()
                                    .progressViewStyle(.circular)
                                    .tint(.more.primary)
                            } else {
                                LoginButton(disabled: .constant(model.token.count == 0)) {
                                    if registration.connected {
                                        model.validate()
                                    } else {
                                        AlertController.shared.openAlertDialog(model: AlertDialogModel(
                                            title: "no_internet_title",
                                            message: "no_internet_message",
                                            confirmLabel: "Ok",
                                            cancelLabel: nil, onConfirm: nil)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    .frame(minHeight: 75)
                }

                Spacer()
                    .frame(maxHeight: .infinity)

                VStack {
                    ExpandableInput(
                        expanded: $showEndpoint,
                        isSmTextfield: .constant(true),
                        headerText: .constant("study_endpoint_headling"),
                        inputPlaceholder: .constant("enter_study_endpoint"),
                        input: $model.endpoint,
                        capitalization: .lowercase,
                        textType: .URL
                    )

                    if !showEndpoint {
                        BasicText(text: "\(model.currentStudyEndpoint())", font: .footnote, lineLimit: 1, textAlign: .center)
                    }
                }
                .padding(.bottom, 8)
                AppVersion()
            }
            .padding(.horizontal, 60)
        }
    }
}

struct LoginView_Previews: PreviewProvider {
    static let database = DatabaseManagerKt.getRoomDatabase(builder: DatabaseManager_iosKt.getDatabaseBuilder())
    static let repos = MainRepository(appDatabase: database)
    static var previews: some View {
        LoginView(registration: RegistrationObservable(service: RegistrationService(shared: Shared(localNotificationListener: LocalPushNotifications(), repositories: repos, sharedStorageRepository: UserDefaultsRepository(), observationDataManager: iOSObservationDataManager(repository: repos), mainBluetoothConnector: IOSBluetoothConnector(), observationFactory: IOSObservationFactory(repository: repos, dataManager: iOSObservationDataManager(repository: repos)), dataRecorder: IOSDataRecorder(), reminderNotificationSchedulingLimit: nil))))
    }
}
