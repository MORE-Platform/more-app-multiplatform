//
//  ExpandableContentWithLink.swift
//  More
//
//  Created by Isabella Aigner on 19.04.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import SwiftUI

struct ExpandableContentWithLink<Content: View>: View {
    @State var content: () -> Content
    @State var title: () -> String
    @Binding var expanded: Bool
    
    var body: some View {
        VStack(alignment: .leading) {
            HStack() {
                SectionHeading(sectionTitle: .constant(title()))
                Spacer()
                UIToggleFoldViewButton(isOpen: $expanded)
            }
            
            Divider().padding(.bottom)
            
            VStack {
                self.content()
            }.padding(0)
                .frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: !expanded ? 0 : .none)
                .clipped()
                .animation(.easeOut)
                .transition(.slide)
        }
    }
}

