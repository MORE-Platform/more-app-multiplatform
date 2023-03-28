//
//  NavigationLinkButton.swift
//  iosApp
//
//  Created by Isabella Aigner on 28.03.23.
//  Copyright © 2023 orgName. All rights reserved.
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
