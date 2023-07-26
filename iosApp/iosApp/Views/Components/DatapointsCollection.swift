//
//  DatapointsCollection.swift
//  iosApp
//
//  Created by Isabella Aigner on 21.03.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import SwiftUI

struct DatapointsCollection: View {
    @Binding var datapoints: Int64
    var running: Bool
    private let stringTable = "TaskDetail"
    var body: some View {
        VStack {
            if running {
                CircleActivityIndicator()
            }
            Title2(titleText: String.localize(forKey: "Collected Datapoints", withComment: "Shows collected Datapoints beneath", inTable: stringTable))
            
            Text(String(datapoints))
                .font(.more.title2)
                .foregroundColor(.more.secondary)
                .fontWeight(.more.title)
        }
    }
}

struct DatapointsCollection_Previews: PreviewProvider {
    static var previews: some View {
        DatapointsCollection(datapoints: .constant(100), running: false)
    }
}
