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

    private let stringsTable = "ConsentView"
    private let taskStringTable = "TaskDetail"
    var body: some View {
        VStack {
            Title2(titleText: .constant(viewModel.permissionModel.studyTitle))
                .padding(.bottom, 30)
            
            ExpandableText(viewModel.permissionModel.studyParticipantInfo, String.localizedString(forKey: "Participant Information", inTable: taskStringTable, withComment: "Participant Information of study."), lineLimit: 4)
                .padding(.bottom, 35)
            
            
            ConsentList(permissionModel: .constant(viewModel.permissionModel))
            Spacer()
            
            
            if viewModel.isLoading {
                ProgressView()
                    .progressViewStyle(.circular)
            } else {
                MoreActionButton(disabled: .constant(false), alertOpen: $viewModel.showErrorAlert) {
                    viewModel.requestPermissions()
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
                                UIApplication.shared.open(url, options: [:], completionHandler: nil)
                            }

                        }),
                        secondaryButton: .cancel({ viewModel.decline() })
                    )
                }
                Spacer()
                MoreActionButton(backgroundColor: .more.important, disabled: .constant(false)) {
                    viewModel.decline()
                } label: {
                    Text(verbatim: .localizedString(
                        forKey: "decline_button",
                        inTable: stringsTable,
                        withComment: "Button to decline the study"))
                }
            }
        }
        .padding(24)
    }
}

struct ConsentView_Previews: PreviewProvider {
    static var previews: some View {
        ConsentView(viewModel: ConsentViewModel(registrationService: RegistrationService(sharedStorageRepository: UserDefaultsRepository())))
    }
}
