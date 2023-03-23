//
//  Accordion.swift
//  iosApp
//
//  Created by Isabella Aigner on 21.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI
import shared

struct AccordionItem: View {
    let title: String
    @Binding var info: String
    @State var isOpen: Bool = false
    
    var body: some View {
        HStack {
            VStack {
                VStack(alignment: .leading) {
                    ConsentListHeader(title: title, hasCheck: .constant(false), isOpen: $isOpen)
                        Divider()
                    Group {
                        if isOpen {
                            BasicText(text: .constant(info), color: .more.secondary)
                        }
                    }
                }
            }
        }
    }
}

struct AccordionItem_Previews: PreviewProvider {
    static var previews: some View {
        AccordionItem(title: "Accordion Title", info: .constant("Accordion Info"))
    }
}
