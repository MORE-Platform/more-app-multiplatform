//
//  NavigationLinkButton.swift
//  iosApp
//
//  Created by Isabella Aigner on 28.03.23.
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

struct NavigationLinkButton<Destination: View, Label: View>: View {
    
    @Binding var disabled: Bool
    var destination: () -> Destination
    var label: () -> Label
    
    var body: some View {
        
        VStack {
            if (!disabled) {
                NavigationLink {
                    destination()
                } label: {
                    label()
                }
                .padding()
                .frame(maxWidth: .infinity)
                .background(
                    RoundedRectangle(cornerRadius: .moreBorder.cornerRadius, style: .continuous)
                        .fill(disabled ? Color.more.primaryLight200 : Color.more.primary)
                )
                .overlay(
                    RoundedRectangle(cornerRadius: .moreBorder.cornerRadius)
                        .stroke(disabled ? Color.more.secondaryMedium : Color.more.primary, lineWidth: 1)
                )
            } else {
                MoreActionButton(disabled: .constant(true)) {
                } label: {
                    label()
                }
            }
            
        }
    }
}
