//
//  ForwardButton.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 07.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI

struct ForwardButton: View {
    var color: Color = .more.primary
    var image = Image(systemName: "chevron.forward")

    var body: some View {
        Button {
        } label: {
            image
        }
        .foregroundColor(color)
    }
}

struct ForwardButton_Previews: PreviewProvider {
    static var previews: some View {
        ForwardButton()
    }
}

