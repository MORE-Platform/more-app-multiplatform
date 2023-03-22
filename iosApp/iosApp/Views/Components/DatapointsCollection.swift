//
//  DatapointsCollection.swift
//  iosApp
//
//  Created by Isabella Aigner on 21.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI

struct DatapointsCollection: View {
    @Binding var datapoints: String
    private let stringTable = "TaskDetail"
    var body: some View {
        VStack {
            CircleActivityIndicator()
            Title2(titleText: .constant(String.localizedString(forKey: "Participant Information", inTable: stringTable, withComment: "Participant Information of specific task.")))
            Title2(titleText: $datapoints, color: .more.secondary)
        }
        
    }
}

struct DatapointsCollection_Previews: PreviewProvider {
    static var previews: some View {
        DatapointsCollection(datapoints: .constant("10.584.773"))
    }
}
