//
//  MoreFilter.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 07.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI

struct MoreFilter<Destination: View>: View {
    @Binding var filterText: String
    var destination: () -> Destination
    var image = Image(systemName: "slider.horizontal.3")
    
    var body: some View {
        NavigationLink {
           destination()
        } label: {
            HStack {
                BasicText(text: filterText)
                image
                    .foregroundColor(Color.more.secondary)
            }
        }
    }
}

struct MoreFilter_Previews: PreviewProvider {
    static var previews: some View {
        MoreFilter(filterText: .constant("test")) {
            EmptyView()
        }
    }
}
