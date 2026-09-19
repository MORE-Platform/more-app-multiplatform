
//
//  MoreFilter.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 07.03.23.
//  Copyright © 2023 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung 
//  Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
//

import SwiftUI

struct MoreFilter: View {
    @Binding var text: String
    var image = Image(systemName: "slider.horizontal.3")

    var body: some View {
        Button {

        } label: {
            HStack {
                BasicText(text: $text)
                image
                    .foregroundColor(Color.more.secondary)
            }
        }
    }
}

struct MoreFilter_Previews: PreviewProvider {
    static var previews: some View {
        MoreFilter(text: .constant("test"))
    }
}