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
                    MoreActionButton(color: .more.importantBright) {
                        viewModel.decline()
                    } label: {
                        Text(verbatim: .localizedString(
                            forKey: "decline_button",
                            inTable: stringsTable,
                            withComment: "Button to decline the study"))
                    }
                    Spacer()
                    MoreActionButton(alertOpen: $viewModel.showErrorAlert) {
                        viewModel.acceptConsent()
                    } label: {
                        Text(verbatim: .localizedString(
                            forKey: "accept_button",
                            inTable: stringsTable,
                            withComment: "Button to accept the study consent"))
                    } errorAlert: {
                        Alert(title:
                            Text(verbatim: .localizedString(
                                forKey: "error_dialog_title",
                                inTable: stringsTable,
                                withComment: "Error dialog title"))
                                .foregroundColor(.more.importantBright),
                            message: Text(viewModel.error),
                            primaryButton: .default(Text(
                                verbatim: .localizedString(
                                    forKey: "dialog_retry",
                                    inTable: stringsTable,
                                    withComment: "Dialog button to retry sending your consent for this study")),
                            action: { viewModel.acceptConsent() }),
                            secondaryButton: .cancel())
                    }
                }
            }
        }
    }
}

struct ConsentView_Previews: PreviewProvider {
    static var previews: some View {
        ConsentView(viewModel: ConsentViewModel(registrationService: RegistrationService(sharedStorageRepository: UserDefaultsRepository())))
    }
}