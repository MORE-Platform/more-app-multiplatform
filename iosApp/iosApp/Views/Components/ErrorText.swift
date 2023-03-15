//
//  ErrorText.swift
//  iosApp
//
//  Created by Jan Cortiel on 09.02.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI

struct ErrorText: View {
    @Binding var message: String
    var body: some View {
        Text(message)
            .foregroundColor(.more.important)
            .fontWeight(.more.error)
            
    }
}

struct ErrorText_Previews: PreviewProvider {
    static var previews: some View {
        ErrorText(message: .constant("System Error!"))
    }
}
