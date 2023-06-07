//
//  LoginView.swift
//  iosApp
//
//  Created by Jan Cortiel on 01.02.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
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
                        
                        VStack {
                            ExpandableInput(
                                expanded: $showEndpoint,
                                isSmTextfield: .constant(true), headerText: .constant(String.localizedString(forKey: "study_endpoint_headling", inTable: stringTable, withComment: "headling for endpoint entryfield")),
                                inputPlaceholder: $model.endpoint,
                                input: $model.endpoint,
                                textType: .URL
                            )
                        }
                    }
                    .padding(.horizontal, 60)
                }
                .viewAdaptsToOpenKeyboard()
                
            }
        
    }
}

struct LoginView_Previews: PreviewProvider {
    static var previews: some View {
        LoginView(model: LoginViewModel(registrationService: RegistrationService(shared: Shared(sharedStorageRepository: UserDefaultsRepository(), observationDataManager: iOSObservationDataManager(), mainBluetoothConnector: IOSBluetoothConnector(), observationFactory: IOSObservationFactory(dataManager: iOSObservationDataManager()), dataRecorder: IOSDataRecorder()))))
    }
}
