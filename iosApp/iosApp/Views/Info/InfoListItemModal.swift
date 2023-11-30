//
//  InfoListItemModal.swift
//  More
//
//  Created by Isabella Aigner on 03.05.23.
//  Copyright © 2023 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung 
//  Licensed under the Apache 2.0 license with Commons Clause 
//  (see https://www.apache.org/licenses/LICENSE-2.0 and
//  https://commonsclause.com/).
//

import Foundation
import SwiftUI

struct InfoListItemModal<Destination: View>: View {
    var title: String
    var icon: String
    var destination: () -> Destination
    let action: () -> Void
    var body: some View {
        VStack(spacing: 14) {
            
            Button(
                action: action,
                label: {
                    HStack {
                        Image(systemName: icon)
                            .foregroundColor(.more.secondary)
                        NavigationText(text: title)
                        Spacer()
                    }
                }
            )
            
            Divider()
        }
    }
}


struct InfoListItemModal_Previews: PreviewProvider {
    static var previews: some View {
        InfoListItem(title: "Test", icon: "info.circle") {
            SettingsView(viewModel: SettingsViewModel())
        }
    }
}

