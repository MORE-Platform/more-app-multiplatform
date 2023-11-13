//
//  LoginButton.swift
//  iosApp
//
//  Created by Jan Cortiel on 06.02.23.
//  Copyright © 2023 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung 
//  Licensed under the Apache 2.0 license with Commons Clause 
//  (see https://www.apache.org/licenses/LICENSE-2.0 and
//  https://commonsclause.com/).
//

import SwiftUI
import shared

struct LoginButton: View {
    @EnvironmentObject var model: LoginViewModel
    @Binding var stringTable: String
    @Binding var disabled: Bool

    var body: some View {
        MoreActionButton(backgroundColor: Color.more.primary, disabled: $disabled) {
            model.validate()
        } label: {
            Text(verbatim:.localize(forKey: "login_button", withComment: "button to log into a more study", inTable: stringTable))
        }
    }
}

struct LoginButton_Previews: PreviewProvider {
    static var previews: some View {
        LoginButton(stringTable: .constant("LoginView"), disabled: .constant(false))
            .environmentObject(LoginViewModel(registrationService: RegistrationService(shared: Shared(localNotificationListener: LocalPushNotifications(), sharedStorageRepository: UserDefaultsRepository(), observationDataManager: ObservationDataManager(), mainBluetoothConnector: IOSBluetoothConnector(), observationFactory: ObservationFactory(dataManager: ObservationDataManager()), dataRecorder: IOSDataRecorder()))))
    }
}
