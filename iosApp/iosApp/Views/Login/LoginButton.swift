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

import shared
import SwiftUI

struct LoginButton: View {
    @Binding var disabled: Bool
    let action: () -> Void

    var body: some View {
        MoreActionButton(backgroundColor: Color.pc.primary, disabled: .constant(disabled)) {
            action()
        } label: {
            Text("login_button")
        }
    }
}

struct LoginButton_Previews: PreviewProvider {
    static let database = DatabaseManagerKt.getRoomDatabase(builder: DatabaseManager_iosKt.getDatabaseBuilder())
    static var previews: some View {
        LoginButton(disabled: .constant(false)) {
            print("Hello World")
        }
    }
}
