//
//  ErrorText.swift
//  iosApp
//
//  Created by Jan Cortiel on 09.02.23.
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

struct ErrorText: View {
    var message: String
    var body: some View {
        Text(message)
            .foregroundColor(.more.important)
            .fontWeight(.more.error)
            
    }
}

struct ErrorText_Previews: PreviewProvider {
    static var previews: some View {
        ErrorText(message: "System Error!")
    }
}
