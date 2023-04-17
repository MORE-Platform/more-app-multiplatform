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
    @Binding var isSelected: Bool
    
    var body: some View {
        Text(text)
            .font(.system(size: 16))
            .font(Font.body.bold())
            .foregroundColor(isSelected ? Color.more.primary : Color.more.secondary)
            .underline(isSelected, color: isSelected ? Color.more.primary : Color.more.secondary)
    }
}


