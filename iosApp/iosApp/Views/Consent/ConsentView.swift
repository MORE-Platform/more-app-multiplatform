//
//  ConsentView.swift
//  iosApp
//
//  Created by Jan Cortiel on 08.02.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import shared
import SwiftUI

struct ConsentView: View {
    @StateObject var viewModel: ConsentViewModel
    @StateObject var permissionManager: PermissionManager
    
    private let stringsTable = "ConsentView"
    var body: some View {
        VStack {
            VStack(alignment: .leading) {
                Title(titleText: .constant(viewModel.permissionModel.studyTitle))
                Divider()
                BasicText(text: .constant(viewModel.permissionModel.studyParticipantInfo))
            }
            
            ConsentList(permissionModel: .constant(viewModel.permissionModel))
            Spacer()
            if viewModel.isLoading {
                ProgressView()
                    .progressViewStyle(.circular)
            } else {
                HStack {
                    MoreActionButton(backgroundColor: .more.important) {
                        viewModel.decline()
                    } label: {
                        Text(verbatim: .localizedString(
                            forKey: "decline_button",
                            inTable: stringsTable,
                            withComment: "Button to decline the study"))
                    }
                    Spacer()
                    MoreActionButton(alertOpen: $viewModel.showErrorAlert) {
                        permissionManager.requestPermission()
                        viewModel.acceptConsent()
                    } label: {
                        Text(verbatim: .localizedString(
                            forKey: "accept_button",
                            inTable: stringsTable,
                            withComment: "Button to accept the study consent"))
                    } errorAlert: {
                        Alert(title:
                                Text(verbatim: .localizedString(
                                    forKey: "permissions_denied",
                                    inTable: stringsTable,
                                    withComment: "Error dialog title"))
                                    .foregroundColor(.more.important),
                              message: Text(viewModel.error),
                              primaryButton: .default(Text(
                                verbatim: .localizedString(
                                    forKey: "to_settings",
                                    inTable: stringsTable,
                                    withComment: "Dialog button to retry sending your consent for this study")),
                            action: {
                                if let url = URL(string: UIApplication.openSettingsURLString), UIApplication.shared.canOpenURL(url) {
                                UIApplication.shared.open(url, options: [:], completionHandler: nil)}
                            
                        }),
                              secondaryButton: .cancel())
                    }
                }
            }
        }
    }
}

struct ConsentView_Previews: PreviewProvider {
    static var previews: some View {
        ConsentView(viewModel: ConsentViewModel(registrationService: RegistrationService(sharedStorageRepository: UserDefaultsRepository())), permissionManager: PermissionManager.permObj)
    }
}
