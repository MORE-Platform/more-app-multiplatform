//
//  MoreFilterOption.swift
//  iosApp
//
//  Created by Isabella Aigner on 28.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI

struct MoreFilterOption: View {
    @Binding var selectedValuesInList: [String]
    @State var underlineText = false
    @State var isSelected = false
    var option: String
    let label: String
    var callback: () -> ()
    
    var body: some View {
        VStack {
            HStack {
                if selectedValuesInList.contains(option) {
                    Image(systemName: "checkmark")
                        .foregroundColor(.more.approved)
                } else {
                    Spacer()
                        .frame(width: 5)
                }
                Button(action: {
                    self.callback()
                }) {
                    MoreFilterText(text: .constant(label), isSelected: $isSelected)
                }
            }
            .padding(5)
        }.onChange(of: selectedValuesInList, perform: { _ in
            isSelected = selectedValuesInList.contains(option)
        })
    }
}

/*
struct Title_Preview: PreviewProvider {
    static var previews: some View {
        Title(titleText: .constant("Hello World"))
    }
}
*/
