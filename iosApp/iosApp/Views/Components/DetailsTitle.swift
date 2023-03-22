//
//  DetailsTitle.swift
//  iosApp
//
//  Created by Daniil Barkov on 22.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI

struct DetailsTitle: View {
    @Binding var text: String
    var color: Color = Color.more.primary
    var font: Font = Font.body
    var weight: Font.Weight = Font.Weight.semibold
    var body: some View {
        Text(text)
            .foregroundColor(color)
            .font(font)
            .fontWeight(weight)    }
}

struct DetailsTitle_Previews: PreviewProvider {
    static var previews: some View {
        DetailsTitle(text: .constant("Hello World"))
    }
}
