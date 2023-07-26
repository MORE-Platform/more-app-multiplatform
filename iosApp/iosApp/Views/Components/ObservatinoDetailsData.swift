//
//  ObservationDetailsData.swift
//  iosApp
//
//  Created by Isabella Aigner on 23.03.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import SwiftUI


struct ObservationDetailsData: View {
    var dateRange: String
    var timeframe: String
    
    private let stringTable = "TaskDetail"
    
    var body: some View {
        
        VStack {
            HStack {
                Image(systemName: "calendar")
                BasicText(text: dateRange, color: .more.secondary)
                    .padding(1)
                Spacer()
            }
            HStack {
                    Image(systemName: "clock.fill")
                        .padding(0.7)
                Text(String.localize(forKey: "Timeframe", withComment: "Timeframe of observation", inTable: stringTable))
                        .foregroundColor(.more.primary)
                    
                    BasicText(text: timeframe, color: .more.secondary)
                    Spacer()
            }
        }
        
    }
}
