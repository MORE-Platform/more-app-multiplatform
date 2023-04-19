//
//  ExpandableContentWithLinks.swift
//  More
//
//  Created by Isabella Aigner on 19.04.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import SwiftUI

struct ExpandableContentWithLinks<Content: View>: View {
    @EnvironmentObject var viewModel: ObservationDetailsViewModel
    @State var content: () -> Content
    @State var title: () -> String
    @State private var expanded: Bool = false
    
    var body: some View {
        VStack(alignment: .leading) {
            HStack() {
                SectionHeading(sectionTitle: .constant(title()))
                Spacer()
                UIToggleFoldViewButton(isOpen: $expanded)
            }
            
            Divider().padding(.bottom)
            
            VStack {
                ZStack {
                    self.content()
                    
                    NavigationLink {
                        ObservationDetailsView(viewModel: viewModel)
                    } label: {
                        EmptyView()
                    }
                    .opacity(0)
                    
                }
                
            }.padding(0)
                .frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: !expanded ? 0 : .none)
                .clipped()
                .animation(.easeOut)
                .transition(.slide)
        }
    }
}
