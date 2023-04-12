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
    
    init(text: Binding<String>, isSelected: Binding<Bool>) {
        self._text = text
        self._isSelected = isSelected
    }
    
    var body: some View {
        if isSelected {
            Text(text)
                .font(.system(size: 16))
                .font(Font.body.bold())
                .foregroundColor(Color.more.primary)
                .underline(true, color: Color.more.primary)
        } else {
            Text(text)
                .font(.system(size: 16))
                .font(Font.body.bold())
                .foregroundColor(Color.more.secondary)
                .underline(false, color: Color.more.secondary)
        }
    }
}


