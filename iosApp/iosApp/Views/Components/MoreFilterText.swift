//
//  MoreFilterText.swift
//  iosApp
//
//  Created by Isabella Aigner on 28.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI

struct MoreFilterText: View {
    @Binding var text: String
    @Binding var color: Color
    @Binding var underline: Bool
    
    var body: some View {
        Text(text)
            .font(.more.title2)
            .foregroundColor(COLOR)
            .fontWeight(.more.title2)
            .underline(underline, color: color)
    }
}

struct Title_Preview: PreviewProvider {
    static var previews: some View {
        Title(text: .constant("Hello World"))
    }
}

