//
//  NotificationView.swift
//  iosApp
//
//  Created by Jan Cortiel on 14.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI

struct NotificationView: View {
    var body: some View {
        Navigation {
            MoreMainBackgroundView {
                Text("Notification View")
            } topBarContent: {
                EmptyView()
            }
            .customNavigationTitle(with: "Notifications")
            .navigationBarTitleDisplayMode(.inline)
        }

    }
}

struct NotificationView_Previews: PreviewProvider {
    static var previews: some View {
        NotificationView()
    }
}
