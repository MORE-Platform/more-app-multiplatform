//
//  LoginViewModel.swift
//  iosApp
//
//  Created by Jan Cortiel on 06.02.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import Foundation
import shared

class LoginViewModel {
    let coreModel = CoreLoginViewModel()
    
    init() {
        coreModel.sendRegistrationToken(token: "")
    }
}
