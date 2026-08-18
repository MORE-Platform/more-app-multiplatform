//
//  ConsentView.swift
//  iosApp
//
//  Created by Jan Cortiel on 08.02.23.
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

struct ConsentView: View {
    @StateObject private var viewModel: ConsentViewModel
    @ObservedObject private var registration: RegistrationObservable

    init(registration: RegistrationObservable) {
        _registration = ObservedObject(wrappedValue: registration)
        _viewModel = StateObject(wrappedValue: ConsentViewModel(registrationService: registration.service))
    }

    var body: some View {
        if let permissionModel = viewModel.permissionModel {
            VStack {
                Title2(titleText: permissionModel.studyTitle)
                    .padding(.bottom, 30)

                ScrollView {
                    ExpandableText(permissionModel.studyParticipantInfo, "Participant Information", lineLimit: 4)
                        .padding(.bottom, 35)

                    ConsentList(permissionModel: permissionModel)
                }

                Spacer()
                if registration.isLoading {
                    ProgressView()
                        .progressViewStyle(.circular)
                        .tint(.more.primary)
                } else {
                    MoreActionButton(disabled: .constant(viewModel.requestedPermissions || registration.isLoading), alertOpen: $viewModel.showErrorAlert) {
                        Napier.event(.buttonPress, message: "Consent accepted")
                        viewModel.requestPermissions()
                    } label: {
                        VStack {
                            if viewModel.requestedPermissions {
                                ProgressView()
                                    .progressViewStyle(.circular)
                                    .tint(.more.primary)
                            } else {
                                Text("accept_button")
                            }
                        }
                    }
                    Spacer()
                    MoreActionButton(backgroundColor: .more.important, disabled: .constant(false)) {
                        Napier.event(.buttonPress, message: "Consent declined")
                        viewModel.decline()
                    } label: {
                        Text("decline_button")
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
        } else {
            StudyLoadingView()
        }
    }
}

#Preview("ConsentView") {
    let database = DatabaseManagerKt.getRoomDatabase(builder: DatabaseManager_iosKt.getDatabaseBuilder())
    let repos = MainRepositoryImpl(appDatabase: database)
    let dataManager = iOSObservationDataManager(repository: repos, scope: Scope.shared, studyScope: StudyScope.shared, dispatchers: AppDispatchers.shared)
    let userDefaults = UserDefaultsRepository()
    let shared = Shared(
        localNotificationListener: LocalPushNotifications(),
        repositories: repos,
        sharedStorageRepository: userDefaults,
        observationDataManager: dataManager,
        mainBluetoothConnector: IOSBluetoothConnector(),
        observationFactory: IOSObservationFactory(repository: repos, dataManager: dataManager, userDefaults: userDefaults),
        dataRecorder: IOSDataRecorder(),
        reminderNotificationSchedulingLimit: nil, connectionStatusFlow: Shared.companion.konnectionInstance().observeHasConnection(), isDebug: true,
        pollingTaskScheduler: nil

    )
    let registration = RegistrationObservable(service: RegistrationService(shared: shared))
    ConsentView(registration: registration)
}
