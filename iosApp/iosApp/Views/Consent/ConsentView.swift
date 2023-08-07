//
//  ConsentView.swift
//  iosApp
//
//  Created by Jan Cortiel on 08.02.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import shared
import SwiftUI

struct ConsentView: View {
    @StateObject var viewModel: ConsentViewModel

    private let stringsTable = "ConsentView"
    private let taskStringTable = "TaskDetail"
    var body: some View {
        VStack {
            Title2(titleText: viewModel.permissionModel.studyTitle)
                .padding(.bottom, 30)

            ExpandableText(viewModel.permissionModel.studyParticipantInfo, String.localize(forKey: "Participant Information", withComment: "Participant Information of study.", inTable: taskStringTable), lineLimit: 4)
                .padding(.bottom, 35)


            ConsentList(permissionModel: viewModel.permissionModel)
            Spacer()
            if viewModel.isLoading {
                ProgressView()
                    .progressViewStyle(.circular)
            } else {
                MoreActionButton(disabled: $viewModel.requestedPermissions, alertOpen: $viewModel.showErrorAlert) {
                    viewModel.requestPermissions()
                } label: {
                    VStack {
                        if viewModel.requestedPermissions {
                            ProgressView()
                                .progressViewStyle(.circular)
                        } else {
                            Text(verbatim: .localize(
                                forKey: "accept_button",
                                withComment: "Button to accept the study consent", inTable: stringsTable))
                        }
                    }
                } errorAlert: {
                    Alert(title:
                        Text(verbatim: .localize(
                            forKey: "permissions_denied",
                            withComment: "Error dialog title", inTable: stringsTable))
                            .foregroundColor(.more.important),
                        message: Text(viewModel.error),
                        primaryButton: .default(Text(
                            verbatim: .localize(
                                forKey: "to_settings",
                                withComment: "Dialog button to retry sending your consent for this study", inTable: stringsTable)),
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
                    Text(verbatim: .localize(
                        forKey: "decline_button",
                        withComment: "Button to decline the study", inTable: stringsTable))
                }
            }
        }
        .padding(24)
        .onAppear {
            viewModel.onAppear()
        }
        .onDisappear {
            viewModel.onDisappear()
        }
    }
}

struct ConsentView_Previews: PreviewProvider {
    static var previews: some View {
        ConsentView(viewModel: ConsentViewModel(registrationService: RegistrationService(shared: Shared(localNotificationListener: LocalPushNotifications(), sharedStorageRepository: UserDefaultsRepository(), observationDataManager: ObservationDataManager(), mainBluetoothConnector: IOSBluetoothConnector(), observationFactory: ObservationFactory(dataManager: ObservationDataManager()), dataRecorder: IOSDataRecorder()))))
    }
}
