//
//  ShowMoreText.swift
//  iosApp
//
//  Created by Isabella Aigner on 23.03.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import SwiftUI


struct ExpandableText: View {
    @State private var expanded: Bool = false
    @State private var truncated: Bool = false
    @State var rotateFold = false
    private var text: String
    private var title: String
    private let stringTable = "ExpandableText"
    private let color: Color
    let lineLimit: Int
    var animation: Animation {
        Animation.easeInOut
    }
    
    init(_ text: String,_ title: String, lineLimit: Int, color: Color = Color.more.secondary) {
        self.text = text
        self.title = title
        self.lineLimit = lineLimit
        self.color = color
    }
    
    private func determineTruncation(_ geometry: GeometryProxy) {
        // Calculate the bounding box we'd need to render the
        // text given the width from the GeometryReader.
        let total = self.text.boundingRect(
            with: CGSize(
                width: geometry.size.width,
                height: .greatestFiniteMagnitude
            ),
            options: .usesLineFragmentOrigin,
            attributes: [.font: UIFont.systemFont(ofSize: 16)],
            context: nil
        )
        
        if total.size.height > geometry.size.height {
            self.truncated = true
        }
    }
    
    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack{
                SectionHeading(sectionTitle: .constant(title))
                Spacer()
                if self.truncated {
                    Button {
                        withAnimation(.easeInOut) { rotateFold.toggle()
                            self.expanded.toggle()
                        }
                    } label: {
                        Image.more.toggleFoldView.rotationEffect(Angle.degrees(rotateFold ? 180 : 0))
                            .animation(animation)
                    }
                }
            }
            
            Text(self.text)
                .foregroundColor(self.color)
                .lineLimit(self.expanded ? nil : self.lineLimit)
                .background(GeometryReader { geometry in
                    Color.clear.onAppear {
                        self.determineTruncation(geometry)
                    }
                })
            
            if self.truncated {
                Button(action: { self.expanded.toggle()
                    rotateFold.toggle()
                }) {
                    Text(self.expanded ? String.localizedString(forKey: "Read Less", inTable: stringTable, withComment: "Read less information") : String.localizedString(forKey: "Read More", inTable: stringTable, withComment: "Read more information"))
                        .font(.system(size: 16))
                }
            }
        }
    }
}

struct ExpandableText_Previews: PreviewProvider {
    static var previews: some View {
        VStack(alignment: .leading, spacing: 10) {
            ExpandableText("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum. Sed ut laborum", "Title", lineLimit: 4)
            ExpandableText("Small text", "Title", lineLimit: 3)
            ExpandableText("Render the limited text and measure its size, R", "Title", lineLimit: 1)
            ExpandableText("Create a ZStack with unbounded height to allow the inner Text as much, Render the limited text and measure its size, Hide the background Indicates whether the text has been truncated in its display.", "Title", lineLimit: 5)
            ExpandableText("Create a ZStack with unbounded height to allow the inner Text as much, Render the limited text and measure its size, Hide the background Indicates whether the text has", "Title", lineLimit: 4)
            ExpandableText("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aenean sed arcu id lacus sollicitudin placerat auctor sed urna. Pellentesque vehicula sollicitudin quam, vitae lacinia sapien porttitor vitae. Donec mattis erat eget elit molestie pellentesque.", "title", lineLimit: 4)
        }.padding()
    }
}
