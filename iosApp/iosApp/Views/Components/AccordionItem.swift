//
//  Accordion.swift
//  iosApp
//
//  Created by Isabella Aigner on 21.03.23.
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
import shared

struct AccordionItem: View {
    let title: String
    var info: String
    @State var isOpen: Bool = false
    
    var body: some View {
        HStack {
            VStack {
                VStack(alignment: .leading) {
                    ConsentListHeader(title: title, hasCheck: .constant(false), isOpen: $isOpen)
                    Divider()
                    if isOpen {
                        BasicText(text: info, color: .more.secondary)
                    }
                }
            }
        }
    }
}

struct AccordionItem_Previews: PreviewProvider {
    static var previews: some View {
        AccordionItem(title: "Accordion Title", info: "Accordion Info")
    }
}
