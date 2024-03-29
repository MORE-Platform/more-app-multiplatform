//
//  MoreFilterOption.swift
//  iosApp
//
//  Created by Isabella Aigner on 28.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI

struct MoreFilterOption: View {
    var option: String
    @Binding var isSelected: Bool
    
    private let stringTable = "DashboardFilter"
    
    var body: some View {
        VStack {
            HStack {
                if isSelected {
                    Image(systemName: "checkmark")
                        .foregroundColor(.more.approved)
                } else {
                    Spacer()
                        .frame(width: 5)
                }
                MoreFilterText(text: .constant(String.localize(forKey: option, withComment: "String representation of observation type", inTable: stringTable)))
            }
            .padding(5)
        }
    }
}
