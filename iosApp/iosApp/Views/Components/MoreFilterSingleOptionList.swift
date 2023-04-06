//
//  MoreFilterSingleOptionList.swift
//  iosApp
//
//  Created by Isabella Aigner on 06.04.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import SwiftUI

struct MoreFitlerSingleOptionList: View {
    @Binding var title: String
    @Binding var optionList: [String]
    private let dashboardStringTable: String = "DashboardFilter"
    
    @State var value: String
    @State var selectedValue: String = ""
    
    let callback: (String, Bool) -> ()
    
    var body: some View {
        VStack(alignment: .leading) {
            /* SectionHeading(sectionTitle: .constant(title))
                .padding(15)
            Divider()
            
            ForEach(optionList, id: \.self) { filter in
                
                MoreFilterOption(
                    label: String.localizedString(forKey: filter, inTable: dashboardStringTable, withComment: "Timeframe filter option"),
                    selected: isSelectedSingleValue(label: filter),
                    callback: {_ in
                        
                        if self.selectedValue == filter {
                            self.selectedValue = "Today and Tomorrow"
                            callback("TODAY_AND_TOMORROW", false)
                        } else {
                            self.selectedValue = String(describing: filter)
                            callback(filter, true)
                        }
                        
                    })
                
                Divider()
                
            } */
            
        }
    }
}

