//
//  RadioButton.swift
//  iosApp
//
//  Created by Isabella Aigner on 27.03.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import SwiftUI

struct RadioButtonField: View {
    let id: String
    let label: String
    let isMarked:Bool
    let callback: (String)->()
    
    init(
        id: String,
        label:String,
        isMarked: Bool = false,
        callback: @escaping (String)->()
        ) {
        self.id = id
        self.label = label
        self.isMarked = isMarked
        self.callback = callback
    }
    
    var body: some View {
        Button(action:{
            self.callback(self.id)
        }) {
            HStack(alignment: .center) {
                Image(systemName: self.isMarked ? "largecircle.fill.circle" : "circle")
                    .clipShape(Circle())
                    .foregroundColor(.more.primary)
                BasicText(text: .constant(label), color: .more.secondary)
                Spacer()
            }.foregroundColor(.more.primaryLight)
        }
        .foregroundColor(.more.white)
        .padding(.bottom, 7)
    }
}

struct RadioButtonField_Preview: PreviewProvider {
    static var previews: some View {
        RadioButtonField(id: "Test", label: "Test", isMarked: false,
                         callback: { selected in
            print("Selected item is \(selected)")
        })
    }
}
