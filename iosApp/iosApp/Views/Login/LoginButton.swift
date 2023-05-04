//
//  LoginButton.swift
//  iosApp
//
//  Created by Jan Cortiel on 06.02.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
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
            Text(verbatim:.localizedString(forKey: "login_button", inTable: stringTable, withComment: "button to log into a more study"))
        }
    }
}

struct LoginButton_Previews: PreviewProvider {
    static var previews: some View {
        LoginButton(stringTable: .constant("LoginView"), disabled: .constant(false))
            .environmentObject(LoginViewModel(registrationService: RegistrationService(shared: Shared(sharedStorageRepository: UserDefaultsRepository()))))
    }
}
