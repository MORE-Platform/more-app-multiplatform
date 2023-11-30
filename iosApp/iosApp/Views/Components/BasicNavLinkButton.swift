//
//  BasicNavLinkButton.swift
//  iosApp
//
//  Created by Daniil Barkov on 18.04.23.
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

struct BasicNavLinkButton<Destination: View, Label: View>: View {
    @Binding var backgroundColor: Color

    var destination: () -> Destination
    var label: () -> Label
        
    var body: some View {
        VStack {
            NavigationLink {
                destination()
            } label: {
                label()
            }
            .padding()
            .frame(maxWidth: .infinity)
            .background(
                RoundedRectangle(cornerRadius: .moreBorder.cornerRadius, style: .continuous)
                    .fill(backgroundColor)
            )
        }
    }
}
