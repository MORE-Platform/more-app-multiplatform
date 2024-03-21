//
//  MoreFilter.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 07.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI

struct MoreFilter: View {
    @Binding var filterText: String
    var destination: NavigationScreen
    var image = Image(systemName: "slider.horizontal.3")
    
    @EnvironmentObject private var navigationModalState: NavigationModalState

    var body: some View {
        Button {
            navigationModalState.openView(screen: destination)
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
        MoreFilter(filterText: .constant("test"), destination: .dashboard)
    }
}
