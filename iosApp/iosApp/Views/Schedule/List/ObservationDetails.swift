//
//  ObservationDetailsButton.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 07.03.23.
//  Copyright © 2023 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung 
//  Licensed under the Apache 2.0 license with Commons Clause 
//  (see https://www.apache.org/licenses/LICENSE-2.0 and
//  https://commonsclause.com/).
//

import SwiftUI

struct ObservationDetails: View {
    let observationTitle: String
    let observationType: String
    let observerHasErrors: Bool
    var action: () -> Void = {}
    
    var body: some View {
        HStack{
            VStack(alignment: .leading) {
                BasicText(text: observationTitle)
                    .font(Font.more.headline)
                    .foregroundColor(Color.more.primary)
                    .padding(.bottom, 1)
                Text(observationType)
                    .foregroundColor(Color.more.secondary)
            }
            .padding(4)
            Spacer()
            if observerHasErrors {
                Image(systemName: "exclamationmark.triangle")
                    .font(.more.headline)
                    .foregroundColor(.more.primary)
                    .padding(.trailing, 4)                
            }
            Image(systemName: "chevron.forward")
        }
    }
}

struct ObservationDetails_Previews: PreviewProvider {
    static var previews: some View {
        ObservationDetails(observationTitle:"Observation Title", observationType: "Observation Type", observerHasErrors: true)
    }
}
