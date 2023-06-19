//
//  ObservationDetailsData.swift
//  iosApp
//
//  Created by Isabella Aigner on 23.03.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import SwiftUI


struct ObservationDetailsData: View {
    @Binding var dateRange: String
    @Binding var timeframe: String
    
    private let stringTable = "TaskDetail"
    
    var body: some View {
        
        VStack {
            HStack {
                Image(systemName: "calendar")
                BasicText(text: .constant(dateRange), color: .more.secondary)
                    .padding(1)
                Spacer()
            }
            HStack {
                    Image(systemName: "clock.fill")
                        .padding(0.7)
                Text(String.localize(forKey: "Timeframe", withComment: "Timeframe of observation", inTable: stringTable))
                        .foregroundColor(.more.primary)
                    
                    BasicText(text: .constant(timeframe), color: .more.secondary)
                    Spacer()
            }
        }
        
    }
}
