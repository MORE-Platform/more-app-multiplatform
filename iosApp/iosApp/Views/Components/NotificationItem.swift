//
//  NotificationItem.swift
//  iosApp
//
//  Created by Isabella Aigner on 12.04.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import SwiftUI

struct NotificationItem: View {
    @Binding var title: String
    @Binding var message: String
    @Binding var read: Bool
    @Binding var isImportant: Bool
    
    var body: some View {
        
        VStack(
            alignment: .leading,
            spacing: 5
        ) {
            HStack {
                if isImportant {
                    Image("more_warning_exclamation_small")
                        .frame(height: 0.5)
                }
                SectionHeading(sectionTitle: .constant(title), font: getFontStyle())
                    .foregroundColor(isImportant ? Color.more.important : Color.more.primary)
                Spacer()
                if !read {
                    Image(systemName: "circle.fill")
                        .foregroundColor(Color.more.important)
                        .font(.system(size: 10))
                }
            }
            Divider()
                .frame(height: 0.5)
            BasicText(text: .constant(message), color: Color.more.secondary)
                .padding(.bottom, 20)
        }
    }

    func getFontStyle() -> Font {
        if self.read {
            return Font.body
        }
        return .more.headline
    }
    
}

struct NotificationItem_Preview: PreviewProvider {
    static var previews: some View {
        NotificationItem(title: .constant("Test Title"), message: .constant("Some message"), read: .constant(false), isImportant: .constant(false))
    }
}


