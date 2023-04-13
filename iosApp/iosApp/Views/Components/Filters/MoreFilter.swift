//
//  MoreFilter.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 07.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI

struct MoreFilter<Destination: View>: View {
    
    @State var text: String = "No Filter Applied"
    @State var typeFilterText: String = ""
    @State var dateFilterText: String = ""
    @EnvironmentObject var filterViewModel: DashboardFilterViewModel
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
        }.onChange(of: filterViewModel.dateFilterString, perform: { _ in
            getFilterText()
        })
        .onChange(of: filterViewModel.observationTypeFilter, perform: { _ in
            getFilterText()
        })
    }
    
    func getFilterText() {
        if filterViewModel.observationTypeFilter.isEmpty && filterViewModel.dateFilterString == "ENTIRE_TIME" {
            text = String.localizedString(forKey: "no_filter_applied", inTable: stringTable, withComment: "No filter set")
        } else {
            if typeFilterSet() {
                if filterViewModel.observationTypeFilter.count == 1 {
                    typeFilterText = "\(filterViewModel.observationTypeFilter.count) Type"
                } else {
                    typeFilterText = "\(filterViewModel.observationTypeFilter.count) Types"
                }
            }
            if dateFilterSet() {
                dateFilterText = String.localizedString(forKey: filterViewModel.dateFilterString, inTable: stringTable, withComment: "Time filter")
            }
            if dateFilterSet() && typeFilterSet() {
                text = "\(typeFilterText), \(dateFilterText)"
            } else if dateFilterSet(){
                text = dateFilterText
            } else {
                text = typeFilterText
            }
        }
    }
    
    func dateFilterSet() -> Bool {
        return filterViewModel.dateFilterString != "ENTIRE_TIME"
    }
    
    func typeFilterSet() -> Bool {
        return !filterViewModel.observationTypeFilter.isEmpty
    }
}

struct MoreFilter_Previews: PreviewProvider {
    static var previews: some View {
        MoreFilter() {
            EmptyView()
        }.environmentObject(DashboardFilterViewModel())
    }
}
