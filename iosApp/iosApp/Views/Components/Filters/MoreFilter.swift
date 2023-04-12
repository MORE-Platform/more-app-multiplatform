//
//  MoreFilter.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 07.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI

struct MoreFilter<Destination: View>: View {
    
    @State var text: String = ""
    var numberOfTypes: Int = 0
    var timeFilter: String = ""
    var destination: () -> Destination
    var stringTable = "DashboardFilter"
    
    var image = Image(systemName: "slider.horizontal.3")
    
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
            getFilterText()
        }
    }
    
    func getFilterText() {
        if numberOfTypes == 0 && timeFilter == "ENTIRE_TIME" {
            text = String.localizedString(forKey: "no_filter_applied", inTable: stringTable, withComment: "No filter set")
        } else {
            if numberOfTypes > 0 {
                if numberOfTypes == 1 {
                    text = "\(numberOfTypes) Type"
                } else {
                    text = "\(numberOfTypes) Types"
                }
            }
            if timeFilter != "ENTIRE_TIME" {
                text = "\(text), \(String.localizedString(forKey: timeFilter, inTable: stringTable, withComment: "Time filter"))"
            }
        }
    }
}

struct MoreFilter_Previews: PreviewProvider {
    static var previews: some View {
        MoreFilter(numberOfTypes: 1, timeFilter: "ENTIRE_TIME") {
            EmptyView()
        }
    }
}
