//
//  ErrorLogin.swift
//  iosApp
//
//  Created by Isabella Aigner on 28.03.23.
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

struct ErrorLogin: View {
    @EnvironmentObject var model: LoginViewModel
    
    @Binding var stringTable: String
    @Binding var disabled: Bool
    
    var body: some View {
        VStack {
            if !model.error.isEmpty {
                ErrorText(message: model.error)
                    .padding(.bottom, 5)
            }
            
            VStack(alignment: .center) {
                if model.isLoading {
                    ProgressView()
                        .progressViewStyle(.circular)
                }
                LoginButton(stringTable: .constant(stringTable), disabled: $disabled)
                    .environmentObject(model)
            }
        }
        .frame(minHeight: 75)
    }
}
