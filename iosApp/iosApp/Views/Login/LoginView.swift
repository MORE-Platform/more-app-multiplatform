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
    @StateObject var model: LoginViewModel
    @State private var rotationAngle = 0.0

    @State private var showTokenInput = true
    @State private var showEndpoint = false

    private let stringTable = "LoginView"

    var body: some View {
        NavigationView {
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
                                    headerText: String.localize(forKey: "participation_key_entry", withComment: "headline for participation token entry field", inTable: stringTable),
                                    inputPlaceholder: .constant(String.localize(forKey: "participation_key_entry", withComment: "headline for participation token entry field", inTable: stringTable)),
                                    input: $model.token,
                                    uppercase: true,
                                    autoCorrectDisabled: true,
                                    textType: .oneTimeCode
                    )
                    .padding(.bottom, 12)

                    if showTokenInput {
                        ErrorLogin(stringTable: .constant(stringTable), disabled: .constant(model.checkTokenCount()))
                            .environmentObject(model)
                    }

                    Spacer()
                        .frame(maxHeight: .infinity)

                    VStack {
                        ExpandableInput(
                            expanded: $showEndpoint,
                            isSmTextfield: .constant(true),
                            headerText: .constant(String.localize(forKey: "study_endpoint_headling", withComment: "headling for endpoint entryfield", inTable: stringTable)),
                            inputPlaceholder: .constant(String.localize(forKey: "enter_study_endpoint", withComment: "Text input field for the study endpoint", inTable: stringTable)),
                            input: $model.endpoint,
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
        .navigationViewStyle(StackNavigationViewStyle())
    }
}

struct LoginView_Previews: PreviewProvider {
    static var previews: some View {
        LoginView(model: LoginViewModel(registrationService: RegistrationService(shared: Shared(localNotificationListener: LocalPushNotifications(), sharedStorageRepository: UserDefaultsRepository(), observationDataManager: iOSObservationDataManager(), mainBluetoothConnector: IOSBluetoothConnector(), observationFactory: IOSObservationFactory(dataManager: iOSObservationDataManager()), dataRecorder: IOSDataRecorder()))))
    }
}
