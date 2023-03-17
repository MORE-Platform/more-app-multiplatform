//
//  InfoListItem.swift
//  iosApp
//
//  Created by Jan Cortiel on 15.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI

struct InfoListItem<Destination: View>: View {
    var title: String
    var icon: String
    var destination: () -> Destination
    var body: some View {
        VStack {
            NavigationLink {
                destination()
            } label: {
                HStack {
                    Image(systemName: icon)
                        .foregroundColor(.more.icons)
                    NavigationText(text: .constant(title))
                    Spacer()
                }
                
            }
            Divider()
        }
    }
}

struct InfoListItem_Previews: PreviewProvider {
    static var previews: some View {
        InfoListItem(title: "Test", icon: "info.circle") {
            SettingsView()
        }
    }
}
