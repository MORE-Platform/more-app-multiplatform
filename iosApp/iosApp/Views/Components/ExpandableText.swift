//
//  ShowMoreText.swift
//  iosApp
//
//  Created by Isabella Aigner on 23.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI

struct ExpandableText: View {
    @State private var expanded: Bool = false
    @State private var truncated: Bool = false
    private var text: String
    private var title: String
    private let stringTable = "ExpandableText"
    
    let lineLimit: Int
    init(_ text: String,_ title: String, lineLimit: Int) {
        self.text = text
        self.title = title
        self.lineLimit = lineLimit
    }
    
    private var moreLessText: String {
        if !truncated {
            return ""
        } else {
            return self.expanded ? String.localizedString(forKey: "Read Less", inTable: stringTable, withComment: "Read less information"): String.localizedString(forKey: "Read More", inTable: stringTable, withComment: "Read more information")
        }
    }
    
    var body: some View {
           VStack(alignment: .leading) {
               HStack{
                   SectionHeading(sectionTitle: .constant(title))
                   Spacer()
                   UIToggleFoldViewButton(isOpen: $expanded)
                       .padding(.bottom, 10)
               }
               Text(text)
                   .lineLimit(expanded ? nil : lineLimit)
                   .background(
                       Text(text).lineLimit(lineLimit)
                           .background(GeometryReader { visibleTextGeometry in
                               ZStack { //large size zstack to contain any size of text
                                   Text(self.text)
                                       .background(GeometryReader { fullTextGeometry in
                                           Color.clear.onAppear {
                                               self.truncated = fullTextGeometry.size.height > visibleTextGeometry.size.height
                                           }
                                       })
                               }
                               .frame(height: .greatestFiniteMagnitude)
                           })
                           .hidden() //keep hidden
               )
               if truncated {
                   Button(action: {
                        expanded.toggle()
                   }, label: {
                       Text(moreLessText)
                           .fontWeight(.more.title)
                           .padding(.top, 5)
                   })
               }
           }
       }
}

struct ExpandableText_Previews: PreviewProvider {
  static var previews: some View {
    VStack(alignment: .leading, spacing: 10) {
        ExpandableText("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum. Sed ut laborum", "Title", lineLimit: 6)
        ExpandableText("Small text", "Title", lineLimit: 3)
        ExpandableText("Render the limited text and measure its size, R", "Title", lineLimit: 1)
        ExpandableText("Create a ZStack with unbounded height to allow the inner Text as much, Render the limited text and measure its size, Hide the background Indicates whether the text has been truncated in its display.", "Title", lineLimit: 3)
    }.padding()
  }
}
