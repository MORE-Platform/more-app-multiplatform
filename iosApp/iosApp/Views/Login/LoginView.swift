//
//  LoginView.swift
//  iosApp
//
//  Created by Jan Cortiel on 01.02.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI
import shared

struct LoginView: View {
    
    @StateObject var model: LoginViewModel
    @State private var rotationAngle = 0.0
    
    @State private var showTokenInput = true
    @State private var showEndpoint = false
    
    private let stringTable = "LoginView"
  
    
    var body: some View {
        NavigationView {
            ZStack {
                Color.more.mainBackground.ignoresSafeArea()
            
                VStack(alignment: .center) {
                    
                    Image("more_welcome")
                        .padding(.vertical, 40)

                    MoreTextFieldHL(isSmTextfield: .constant(false),
                                    headerText: .constant(String.localizedString(forKey: "participation_key_entry", inTable: stringTable, withComment: "headline for participation token entry field")),
                                    inputPlaceholder: .constant(String.localizedString(forKey: "participation_key_entry", inTable: stringTable, withComment: "headline for participation token entry field")),
                                    input: $model.token)
                    .padding(.bottom, 12)
                    
                    if showTokenInput {
                        ErrorLogin(stringTable: .constant(stringTable), disabled: .constant(model.checkTokenCount()))
                            .environmentObject(model)
                    }
                        
                    VStack{
                        Text(String.localizedString(forKey: "or", inTable: stringTable, withComment: "Choose either or of the two options."))
                            .fontWeight(.more.title)
                            
                    }
                    .padding(25)
                   
                    NavigationLinkButton  {
                        LoginQRCodeView(model: model)
                    } label: {
                        HStack {
                            Text(String.localizedString(forKey: "qr_code_entry", inTable: stringTable, withComment: "Click to Scan QR Code to log in."))
                                .foregroundColor(.more.white)
                            Spacer()
                            Image(systemName: "chevron.forward")
                                    .foregroundColor(.more.white)
                        }
                    }
                    .padding(.vertical, 15)	
                    
                    Spacer()
                    
                    VStack {
                        ExpandableInput(
                            expanded: $showEndpoint,
                            isSmTextfield: .constant(true), headerText: .constant(String.localizedString(forKey: "study_endpoint_headling", inTable: stringTable, withComment: "headling for endpoint entryfield")),
                            inputPlaceholder: $model.endpoint,
                            input: $model.endpoint
                        )
                    }
                }
                .padding(.horizontal, 60)
            }
            
        }
    }
}

struct LoginView_Previews: PreviewProvider {
    static var previews: some View {
        LoginView(model: LoginViewModel(registrationService: RegistrationService(sharedStorageRepository: UserDefaultsRepository())))
    }
}
