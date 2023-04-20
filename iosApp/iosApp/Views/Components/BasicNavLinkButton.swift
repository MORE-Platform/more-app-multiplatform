//
//  BasicNavLinkButton.swift
//  iosApp
//
//  Created by Daniil Barkov on 18.04.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
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
