//
//  NotificationItem.swift
//  iosApp
//
//  Created by Isabella Aigner on 12.04.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import SwiftUI

struct NotificationItem: View {
    var title: String
    var message: String
    var read: Bool
    var isImportant: Bool
    var timestamp: Int64

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
                SectionHeading(sectionTitle: title, font: getFontStyle())
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
            BasicText(text: message, color: Color.more.secondary)
            BasicText(text: (timestamp / 1000).toDateString(dateFormat: "dd.MM.yyyy HH:mm"))
                .padding(.vertical)
        }
    }

    func getFontStyle() -> Font {
        if read {
            return Font.body
        }
        return .more.headline
    }
}

struct NotificationItem_Preview: PreviewProvider {
    static var previews: some View {
        NotificationItem(title: "Test Title", message: "Some message", read: false, isImportant: false, timestamp: 0)
    }
}
