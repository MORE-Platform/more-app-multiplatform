//
//  CheckBoxField.swift
//  More
//
//  Created by dhp lbi on 25.09.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import SwiftUI

struct CheckBoxField: View {
    let id: String
    let label: String
    let isChecked:Bool
    let callback: (String, Bool)->()
    
    init(
        id: String,
        label:String,
        isChecked: Bool = false,
        callback: @escaping (String, Bool)->()
        ) {
        self.id = id
        self.label = label
        self.isChecked = isChecked
        self.callback = callback
    }
    
    var body: some View {
        Button(action:{
            self.callback(self.id, !self.isChecked)
        }) {
            HStack(alignment: .center) {
                Image(systemName: self.isChecked ? "checkmark.square.fill" : "square")
                    .resizable()
                    .frame(width: 30, height: 30)
                    .clipShape(Rectangle())
                    .foregroundColor(.more.primary)
                BasicText(text: label, color: .more.secondary)
                Spacer()
            }.foregroundColor(.more.primaryLight)
        }
        .foregroundColor(.more.white)
        .padding(.bottom, 7)
    }
}

struct CheckBoxField_Preview: PreviewProvider {
    static var previews: some View {
        CheckBoxField(id: "Test", label: "Test", isChecked: false) {id, isChecked in
            print("CheckBox with ID \(id) is now \(isChecked ? "checked" : "unchecked")")
        }
    }
}
