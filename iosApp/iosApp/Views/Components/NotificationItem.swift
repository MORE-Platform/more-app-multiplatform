//
//  NotificationItem.swift
//  iosApp
//
//  Created by Isabella Aigner on 12.04.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import SwiftUI
import shared

struct NotificationItem: View {
    let notificationModel: NotificationModel

    var body: some View {
        VStack(
            alignment: .leading
        ) {
            HStack {
                if notificationModel.priority == 2 {
                    Image("more_warning_exclamation_small")
                        .frame(height: 0.5)
                }
                SectionHeading(sectionTitle: notificationModel.title, font: getFontStyle())
                    .foregroundColor(notificationModel.priority == 2 ? Color.more.important : Color.more.primary)
                Spacer()
                if !notificationModel.read {
                    Image(systemName: "circle.fill")
                        .foregroundColor(Color.more.important)
                        .font(.system(size: 10))
                }
            }
            Divider()
                .frame(height: 0.5)
                .padding(.vertical, 4)
            HStack(alignment: .center) {
                VStack(alignment: .leading) {
                    BasicText(text: notificationModel.notificationBody, color: .more.secondary)
                        .padding(.bottom, 2)
                    BasicText(text: (notificationModel.timestamp / 1000).toDateString(dateFormat: "dd.MM.yyyy HH:mm:ss"))
                }
                Spacer()
                if notificationModel.deepLink != nil {
                    Image(systemName: notificationModel.read ? "checkmark.circle" : "chevron.right")
                        .foregroundColor(notificationModel.read ? .more.approved : .more.secondary)
                }
            }
        }
        .padding(.bottom)
    }

    func getFontStyle() -> Font {
        if notificationModel.read {
            return Font.body
        }
        return .more.headline
    }
}

struct NotificationItem_Preview: PreviewProvider {
    static var previews: some View {
        NotificationItem(notificationModel: NotificationModel(notificationId: "abc2", channelId: nil, title: "Title", notificationBody: "Message", timestamp: Int64(Date().timeIntervalSince1970), priority: 2, read: true, userFacing: true, deepLink: "app://", notificationData: [:]))
    }
}
