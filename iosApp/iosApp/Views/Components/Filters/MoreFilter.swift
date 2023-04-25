//
//  MoreFilter.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 07.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI

struct MoreFilter<Destination: View>: View {
    @EnvironmentObject var viewModel: DashboardFilterViewModel
    var destination: () -> Destination
    var image = Image(systemName: "slider.horizontal.3")
    @State private var text = ""
    
    var body: some View {
        NavigationLink {
           destination()
        } label: {
            HStack {
                BasicText(text: $text)
                image
                    .foregroundColor(Color.more.secondary)
            }
        }.onAppear {
            text = viewModel.updateFilterText()
        }
    }
}

struct MoreFilter_Previews: PreviewProvider {
    static var previews: some View {
        MoreFilter() {
            EmptyView()
        }
    }
}
