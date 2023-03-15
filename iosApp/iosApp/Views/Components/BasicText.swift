//
//  BasicText.swift
//  iosApp
//
//  Created by Jan Cortiel on 07.02.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI

struct BasicText: View {
    @Binding var text: String
    var body: some View {
        Text(text)
            .foregroundColor(.more.primary)
    }
}

struct BasicText_Previews: PreviewProvider {
    static var previews: some View {
        BasicText(text: .constant("Hello World"))
    }
}
