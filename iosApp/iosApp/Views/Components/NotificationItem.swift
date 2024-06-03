//
//  NotificationItem.swift
//  iosApp
//
//  Created by Isabella Aigner on 12.04.23.
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
            
            HStack(alignment: .center) {
                VStack(alignment: .leading) {
                    BasicText(text: notificationModel.notificationBody.applyHyperlinks().trimmingCharacters(in: .whitespacesAndNewlines), color: .more.secondary)
                        
                    BasicText(text: (notificationModel.timestamp / 1000).toDateString(dateFormat: "dd.MM.yyyy HH:mm:ss"))
                        .padding(.top, 4)
                }
                Spacer()
                if notificationModel.deepLink != nil {
                    Image(systemName: notificationModel.read ? "checkmark.circle" : "chevron.right")
                        .foregroundColor(notificationModel.read ? .more.approved : .more.secondary)
                }
            }
        }
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
