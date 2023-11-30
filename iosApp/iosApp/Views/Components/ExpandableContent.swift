//
//  Collapsible.swift
//  iosApp
//
//  Created by Daniil Barkov on 30.03.23.
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

struct ExpandableContent<Content: View>: View {
    @State var content: () -> Content
    @State var title: () -> String
    @State private var expanded: Bool = false
    
    var body: some View {
        VStack(alignment: .leading) {
            HStack() {
                SectionHeading(sectionTitle: title())
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
