//
//  StudyDetailsViewModel.swift
//  iosApp
//
//  Created by Daniil Barkov on 22.03.23.
//  Copyright © 2023 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung
//  Licensed under the Apache 2.0 license with Commons Clause
//  (see https://www.apache.org/licenses/LICENSE-2.0 and
//  https://commonsclause.com/).
//

import Combine
import KMPNativeCoroutinesCombine
import shared

class StudyDetailsViewModel: ObservableObject {
    private let coreModel = CoreStudyDetailsViewModel(shared: AppDelegate.shared, customViewIdentifier: nil)
    @Published var studyDetailsModel: StudyDetailsModel?
    var studyStart: Date = Date()
    var studyEnd: Date = Date()

    private var cancellables = Set<AnyCancellable>()

    init() {
        createPublisher(for: coreModel.studyModel)
        .receive(on: DispatchQueue.main)
        .sink(receiveCompletion: { _ in }) { [weak self] studyDetails in
            self?.studyDetailsModel = studyDetails
            if let studyDetailsModel = studyDetails {
                if let start = studyDetailsModel.study.start?.toInt64().toDate() {
                    self?.studyStart = start
                }
                if let end = studyDetailsModel.study.end?.toInt64().toDate() {
                    self?.studyEnd = end
                }
            }
        }
        .store(in: &cancellables)
    }

    func viewDidAppear() {
        coreModel.viewDidAppear()
    }

    func viewDidDisappear() {
        coreModel.viewDidDisappear()
        studyDetailsModel = nil
    }
}
