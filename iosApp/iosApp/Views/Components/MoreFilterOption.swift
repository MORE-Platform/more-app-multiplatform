//
//  MoreFilterOption.swift
//  iosApp
//
//  Created by Isabella Aigner on 28.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI

struct MoreFilterOption: View {
    let selected: Bool
    let label: String
    let callback: (String) -> ()
    
    init(
        label: String,
        selected: Bool = false,
        callback: @escaping (String) -> ()
    ) {
        self.label = label
        self.selected = selected
        self.callback = callback
    }
    
    var body: some View {
        VStack {
            HStack {
                if selected {
                    Image(systemName: "checkmark")
                        .foregroundColor(.more.approved)
                } else {
                    Spacer()
                        .frame(width: 5)
                }
                Button(action: {
                    self.callback(self.label)
                }) {
                    MoreFilterText(text: .constant(label), color: self.selected ? .constant(.more.primary) : .constant(.more.secondary), underline: .constant(self.selected))
                }
            }
            .padding(5)
        }
    }
}

/*
struct Title_Preview: PreviewProvider {
    static var previews: some View {
        Title(titleText: .constant("Hello World"))
    }
}
*/
