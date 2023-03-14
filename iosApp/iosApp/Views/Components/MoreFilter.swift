//
//  MoreFilter.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 07.03.23.
//  Copyright © 2023 orgName. All rights reserved.
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
