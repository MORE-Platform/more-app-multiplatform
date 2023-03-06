//
//  ScheduleList.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 03.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI
import shared

struct ScheduleList: View {
    var model: DashboardViewModel
    var dummyList: [Int] = [1, 2, 3]
    var body: some View {
        List {
            ForEach(dummyList, id: \.self) { item in
                VStack {
                    VStack(alignment: .leading) {
                        Text("Title")
                        Button {
                            
                        } label: {
                            HStack {
                                Text("subtitle")
                                Spacer()
                                Image(systemName: "chevron.forward")
                            }
                        }.buttonStyle(PlainButtonStyle())
                    }
                    HStack {
                        Image(systemName: "clock.fill")
                        Text("Start")
                        Spacer()
                        Image(systemName: "clock.fill")
                        Text("Active for: ")
                        Text("30 min")
                    }
                    MoreActionButton {
                        
                    } label: {
                        Text("Start Observation")
                    }.buttonStyle(PlainButtonStyle())
                }
            }
        }.listStyle(.plain)
    }
}

struct ScheduleList_Previews: PreviewProvider {
    static var previews: some View {
        ScheduleList(model: DashboardViewModel())
    }
}


