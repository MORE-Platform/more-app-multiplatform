//
//  InfoListItemModal.swift
//  More
//
//  Created by Isabella Aigner on 03.05.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import Foundation
import SwiftUI

struct InfoListItemModal<Destination: View>: View {
    @EnvironmentObject var leaveStudyModalStateVM: LeaveStudyModalStateViewModel
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
                        NavigationText(text: .constant(title))
                        Spacer()
                    }
                }
            )
            .sheet(isPresented: $leaveStudyModalStateVM.isLeaveStudyOpen) {
                destination().environmentObject(leaveStudyModalStateVM)
            }
            
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

