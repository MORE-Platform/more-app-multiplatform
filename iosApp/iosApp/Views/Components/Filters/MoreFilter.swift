//
//  MoreFilter.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 07.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI

struct MoreFilter<Destination: View>: View {
    
    @EnvironmentObject var filterViewModel: DashboardFilterViewModel
    var destination: () -> Destination
    
    @State var text: String = ""
    @State var typeFilterText: String = ""
    @State var dateFilterText: String = ""
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
        if noFilterSet() {
            text = String.localizedString(forKey: "no_filter_activated", inTable: stringTable, withComment: "No filter set")
        } else {
            if typeFilterSet() {
                if filterViewModel.observationTypeFilter.count == 1 {
                    typeFilterText = "\(filterViewModel.observationTypeFilter.count) \(String.localizedString(forKey: "type", inTable: stringTable, withComment: "Observation type"))"
                } else {
                    typeFilterText = "\(filterViewModel.observationTypeFilter.count) \(String.localizedString(forKey: "type_plural", inTable: stringTable, withComment: "Observation types"))"
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
    
    func noFilterSet() -> Bool {
        return !dateFilterSet() && !typeFilterSet()
    }
}

struct MoreFilter_Previews: PreviewProvider {
    static var previews: some View {
        MoreFilter() {
            EmptyView()
        }.environmentObject(DashboardFilterViewModel())
    }
}
