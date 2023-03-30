//
//  MoreFilterOption.swift
//  iosApp
//
//  Created by Isabella Aigner on 28.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI

struct MoreFilterOption: View {
    @State var selected: Bool
    @Binding var label: String
    
    var body: some View {
        VStack {
            HStack {
                if selected {
                    Image(systemName: "checkmark")
                        .frame(width: 15)
                } else {
                    Spacer()
                        .frame(width: 15)
                }
                
                MoreFilterText(text: label, color: selected ? .more.primary : .more.secondary, underline: selected)
            }
        }
    }
}

struct Title_Preview: PreviewProvider {
    static var previews: some View {
        Title(titleText: .constant("Hello World"))
    }
}

