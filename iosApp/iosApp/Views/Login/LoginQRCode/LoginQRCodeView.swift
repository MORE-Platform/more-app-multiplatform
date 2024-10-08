//
//  LoginQRCodeView.swift
//  iosApp
//
//  Created by Isabella Aigner on 24.03.23.
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


struct LoginQRCodeView: View {
    @StateObject var model: LoginViewModel
    private let navigationStrings = "Navigation"
    private let stringTable = "LoginView"
    
    @Environment(\.presentationMode) var presentationMode
    
    var body: some View {
        
        
        VStack(alignment: .center) {
            Image("more_welcome")
                .padding(.top, 15)
                .padding(.bottom, 40)
            
            BasicText(text: String.localize(forKey: "scan_qr_code", withComment: "Login with QR Code.", inTable: stringTable))
                .padding(.bottom, 5)
            
            RoundedRectangle(cornerRadius: .moreBorder.cornerRadius, style: .continuous)
                .fill(Color.more.white)
                .frame(height: 300)
                .padding(.bottom, 12)
            
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
                }
            }
            .frame(minHeight: 20)
            .padding(.bottom, 20)
            
            MoreActionButton(backgroundColor: .more.secondary, disabled: .constant(false)){
                
                self.presentationMode.wrappedValue.dismiss()
                
            } label: {
                VStack {
                    Text("Close")
                }
            }
            
            Spacer()
        }
        .customNavigationTitle(with: NavigationScreen.scanQRCode.localize(useTable: navigationStrings, withComment: "Scan QR Code to Login"))
    }
}
