//
//  ObservationDetailsButton.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 07.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI

struct ObservationDetailsButton: View {
    @State var observationTitle: String
    @State var observationType: String
    var action: () -> Void = {}
    
    var body: some View {
        Button {
            action()
        } label: {
            HStack{
                VStack(alignment: .leading) {
                    BasicText(text: $observationTitle)
                        .font(Font.more.headline)
                        .foregroundColor(Color.more.main)
                    BasicText(text: $observationType)
                        .foregroundColor(Color.more.icons)
                }
                Spacer()
                Image(systemName: "chevron.forward")
            }
        }
    }
}

struct ObservationDetailsButton_Previews: PreviewProvider {
    static var previews: some View {
        ObservationDetailsButton(observationTitle:"Observation Title", observationType: "Observation Type")
    }
}
