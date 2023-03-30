//
//  MoreFilterList.swift
//  iosApp
//
//  Created by Isabella Aigner on 28.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI

struct MoreFilterOptionList: View {
    
    /*@Binding var multiSelect = false
    @Binding var title: String
    @Binding var filterList: []
    */
    var body: some View {
       
        VStack(alignment: .leading) {
            Title2(titleText: .constant("test"))
            /*
            ForEach(filterList, id: \.self) { filter in
                MoreFilterOption(selected: filter.selected, label: filter.label)
                Divider()
            }*/
        }
    }
}
/*
struct MoreFilterOptionList_Preview: PreviewProvider {
    static var previews: some View {
        MoreFilterOptionList(title: "Filter List", filterList: [{selected: true, label: "Today"}, {selected: false, label: "Today and Tomorrow"}])
    }
}

*/
