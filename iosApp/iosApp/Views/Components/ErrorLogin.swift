//
//  ErrorLogin.swift
//  iosApp
//
//  Created by Isabella Aigner on 28.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI

struct ErrorLogin: View {
    @EnvironmentObject var model: LoginViewModel
    
    @Binding var stringTable: String
    @Binding var disabled: Bool
    
    var body: some View {
        VStack {
            if !model.error.isEmpty {
                ErrorText(message: $model.error)
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
