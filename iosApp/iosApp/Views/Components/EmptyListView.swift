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
    var body: some View {
        DetailsTitle(text: text, color: .more.textDefault)
            .padding(32)
    }
}

struct EmptyListView_Previews: PreviewProvider {
    static var previews: some View {
        EmptyListView(text: "Empty list")
    }
}
