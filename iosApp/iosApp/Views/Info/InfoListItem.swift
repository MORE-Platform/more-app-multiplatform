//
//  InfoListItem.swift
//  iosApp
//
//  Created by Jan Cortiel on 15.03.23.
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

struct InfoListItem<Destination: View>: View {
    var title: String
    var icon: String
    var destination: () -> Destination
    var body: some View {
        VStack(spacing: 14) {
            NavigationLink {
                destination()
            } label: {
                HStack {
                    Image(systemName: icon)
                        .foregroundColor(.more.secondary)
                    NavigationText(text: title)
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
            SettingsView(viewModel: SettingsViewModel())
        }
    }
}
