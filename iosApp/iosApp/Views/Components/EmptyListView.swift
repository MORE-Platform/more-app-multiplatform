//
//  EmptyListView.swift
//  More
//
//  Created by Jan Cortiel on 24.04.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import SwiftUI

struct EmptyListView: View {
    var text: String
    var font: Font = Font.body
    var body: some View {
        DetailsTitle(text: text, color: .more.textDefault, font: font)
            .padding(.vertical, 16)
    }
}

struct EmptyListView_Previews: PreviewProvider {
    static var previews: some View {
        EmptyListView(text: "Empty list")
    }
}
