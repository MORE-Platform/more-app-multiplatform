//
//  ObservationDetailsButton.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 07.03.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import SwiftUI

struct ObservationDetails: View {
    @State var observationTitle: String
    @State var observationType: String
    var action: () -> Void = {}
    
    var body: some View {
            HStack{
                VStack(alignment: .leading) {
                    BasicText(text: $observationTitle)
                        .font(Font.more.headline)
                        .foregroundColor(Color.more.primary)
                        .padding(0.5)
                    Text(observationType)
                        .foregroundColor(Color.more.secondary)
                }
                Spacer()
                Image(systemName: "chevron.forward")
            }
            .contentShape(Rectangle())
    }
}

struct ObservationDetails_Previews: PreviewProvider {
    static var previews: some View {
        ObservationDetails(observationTitle:"Observation Title", observationType: "Observation Type")
    }
}
