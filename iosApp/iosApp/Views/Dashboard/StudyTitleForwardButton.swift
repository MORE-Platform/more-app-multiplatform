//
//  StudyTitleForwardButton.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 07.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI

struct StudyTitleForwardButton: View {
    @Binding var title: String
    let action: () -> Void = {}
    
    var body: some View {
        Button {
            action()
        } label: {
            HStack {
                Title(titleText: $title)
                Spacer()
                Image(systemName: "chevron.forward")
            }
        }
    }
}

struct StudyTitleForwardButton_Previews: PreviewProvider {
    static var previews: some View {
        StudyTitleForwardButton(title: .constant("Study Title"))
    }
}
