//
//  EmptyListView.swift
//  More
//
//  Created by Jan Cortiel on 24.04.23.
//  Copyright © 2023 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung 
//  Licensed under the Apache 2.0 license with Commons Clause 
//  (see https://www.apache.org/licenses/LICENSE-2.0 and
//  https://commonsclause.com/).
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
