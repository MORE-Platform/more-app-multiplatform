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
    @State private var endpointShowTextField = false

    
    private let stringTable = "LoginView"
  
    
    var body: some View {
        NavigationView {
            ZStack {
                Color.more.mainBackground.ignoresSafeArea()
            
                VStack(alignment: .center) {
                    
                    Image("more_welcome")
                        .padding(.vertical, 40)

                    VStack {
                        VStack(alignment: .leading) {
                            
                            HStack{
                                Spacer()
                                SectionHeading(sectionTitle: .constant(.localizedString(forKey: "participation_key_entry", inTable: stringTable, withComment: "headline for participation token entry field")))
                                UIToggleFoldViewButton(isOpen: $showTokenInput)
                                Spacer()
                            }
                            .padding(3)
                            
                            if showTokenInput {
                                MoreTextField(titleKey: .constant(.localizedString(forKey: "participation_key_entry", inTable: stringTable, withComment: "headline for participation token entry field")), inputText: $model.token)
                            }
                            
                        }
                    }
                    .padding(.bottom, 12)
                    
                    if showTokenInput {
                        VStack {
                            if !model.error.isEmpty {
                                ErrorText(message: $model.error)
                                    .padding(.bottom, 5)
                            }
                            
                            VStack(alignment: .center) {
                                if !model.isLoading {
                                    LoginButton(stringTable: .constant(stringTable))
                                        .environmentObject(model)
                                } else {
                                    ProgressView()
                                        .progressViewStyle(.circular)
                                }
                            }
                        }
                        .frame(minHeight: 75)
                    }
                        
                    
                    VStack{
                        Text(String.localizedString(forKey: "or", inTable: stringTable, withComment: "Choose either or of the two options."))
                            .fontWeight(.more.title)
                            
                    }
                    .padding(25)
                   
                    VStack {
                        NavigationLink {
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
                        .padding()
                        .frame(maxWidth: .infinity)
                        .background(
                            RoundedRectangle(cornerRadius: .moreBorder.cornerRadius, style: .continuous).fill(Color.more.primary)
                        )
                    }
                    .padding(.vertical, 15)
                    
                    Spacer()
                    
                    VStack {
                        HStack {
                            BasicText(text: .constant(.localizedString(forKey: "study_endpoint_headling", inTable: stringTable, withComment: "headling for endpoint entryfield")));
                        
                            UIToggleFoldViewButton(isOpen: $endpointShowTextField)
                                
                        }
                        .padding(.bottom, 1)
                        
                        VStack {
                            if endpointShowTextField {
                                MoreTextFieldSmBottom(titleKey: $model.endpoint, inputText: $model.endpoint)
                            }
                        }
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
