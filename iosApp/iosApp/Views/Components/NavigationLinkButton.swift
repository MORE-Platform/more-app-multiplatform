//
//  NavigationLinkButton.swift
//  iosApp
//
//  Created by Isabella Aigner on 28.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI

struct NavigationLinkButton<Destination: View, Label: View>: View {
    
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
                RoundedRectangle(cornerRadius: .moreBorder.cornerRadius, style: .continuous).fill(Color.more.primary)
            )
        }
    }
}
