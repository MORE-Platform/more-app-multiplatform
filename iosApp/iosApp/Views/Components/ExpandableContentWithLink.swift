//
//  ExpandableContentWithLink.swift
//  More
//
//  Created by Isabella Aigner on 19.04.23.
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

struct ExpandableContentWithLink<Content: View>: View {
    @State var content: () -> Content
    @State var title: () -> String
    @Binding var expanded: Bool
    
    var body: some View {
        VStack(alignment: .leading) {
            HStack() {
                SectionHeading(sectionTitle: title())
                Spacer()
                UIToggleFoldViewButton(isOpen: $expanded)
            }
            .contentShape(Rectangle())
            .onTapGesture {
                withAnimation {
                    expanded.toggle()
                }
            }
            
            Divider().padding(.bottom)
            
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
        .animation(.easeOut(duration: 0.3), value: expanded)
    }
}

struct ExpandableContentWithLink_Previews: PreviewProvider {
    static var previews: some View {
        ExpandableContentWithLink(content: { Text("Hello, World!") }, title: { "Hello" }, expanded: .constant(false))
    }
}
