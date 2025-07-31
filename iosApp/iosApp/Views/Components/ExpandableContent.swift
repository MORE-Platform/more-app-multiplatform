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
            
            Divider()
            
            if expanded {
                VStack {
                    self.content()
                }
                .frame(maxWidth: .infinity)
                .multilineTextAlignment(.center)
                .transition(.opacity.combined(with: .scale))
                .padding(.top, 8)
            }

        }
        .padding(.bottom)
        .animation(.easeOut(duration: 0.3), value: expanded)
    }
}


struct ExpandableContent_Preview: PreviewProvider {
    static var previews: some View {
        ExpandableContent(content: {
            Text("Hello, World!")
        }) {
            "Hello"
        }
    }
}
