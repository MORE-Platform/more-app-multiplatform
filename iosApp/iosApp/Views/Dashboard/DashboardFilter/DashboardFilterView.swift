//
//  DashboardFilterView.swift
//  iosApp
//
//  Created by Isabella Aigner on 28.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI
import shared

struct DashboardFitlerView: View {
    @StateObject var dashboardFilterViewModel: DashboardFilterViewModel
    
    @State var dateFilter: String = "Today"
    @State var observationTypeFilter: Set<String> = ["All Items"]
    let stringTable = "DashboardFilter"
    
    var body: some View {
        Navigation {
            MoreMainBackgroundView {
                VStack {
                    VStack(alignment: .leading) {
                        MoreFilterOptionList(title: constant(String.localizedString(forKey: "Set duration", inTable: stringTable, withComment: "Set the duration of the schedule time frame.")), filterList: .constant(dashboardFilterViewModel.dateFilterOptions))
                        }
                    }
                }
            }
        }
    }
}
