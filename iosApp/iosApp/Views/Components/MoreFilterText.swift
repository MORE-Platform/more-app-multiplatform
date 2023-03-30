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
            .foregroundColor(color)
            .fontWeight(.more.title)
            .underline(underline, color: color)
    }
}


