//
//  LoginButton.swift
//  iosApp
//
//  Created by Jan Cortiel on 06.02.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI

struct LoginButton: View {
    @EnvironmentObject var model: LoginViewModel
    @Binding var key: String
    @Binding var endpoint: String
    @Binding var stringTable: String
    var body: some View {
        MoreActionButton {
            model.validate(token: key, endpoint: endpoint)
        } label: {
            Text(verbatim:.localizedString(forKey: "login_button", inTable: stringTable, withComment: "button to log into a more study"))
        }
    }
}

struct LoginButton_Previews: PreviewProvider {
    static var previews: some View {
        LoginButton(key: .constant(""), endpoint: .constant(""), stringTable: .constant("LoginView"))
            .environmentObject(LoginViewModel())
    }
}
