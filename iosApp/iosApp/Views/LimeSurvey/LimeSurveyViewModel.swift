//
//  LimeSurveyViewModel.swift
//  More
//
//  Created by Jan Cortiel on 10.05.23.
//  Copyright © 2023 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung
//  Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
//

import Foundation
import shared
import WebKit
import Combine
import KMPNativeCoroutinesCombine

class LimeSurveyViewModel: ObservableObject {
    private let coreViewModel: CoreLimeSurveyViewModel
    let webViewModel = WebViewViewModel()

    @Published var limeSurveyLink: URL?
    @Published var dataLoading = false
    @Published var wasAnswered = false
    @Published var shouldClose = false

    private var cancellables = Set<AnyCancellable>()

    init(navigationState: NavigationState) {
        coreViewModel = CoreLimeSurveyViewModel(repositories: AppDelegate.shared.repositories, observationFactory: AppDelegate.shared.observationFactory, scheduleId: navigationState.scheduleId, notificationId: navigationState.notificationId, observationId: navigationState.observationId)
        webViewModel.delegate = self
        
        createPublisher(for: coreViewModel.dataLoading)
            .receive(on: DispatchQueue.main)
            .sink(receiveCompletion: {_ in}) { [weak self] in
                self?.dataLoading = $0.boolValue
            }
            .store(in: &cancellables)
        
        createPublisher(for: coreViewModel.limeSurveyLink)
            .removeDuplicates()
            .receive(on: DispatchQueue.main)
            .sink(receiveCompletion: {_ in}) { [weak self] in
                self?.limeSurveyLink = if let link = $0 {
                    URL(string: link)
                } else {
                    nil
                }
            }
            .store(in: &cancellables)
    }

    func viewDidAppear() {
        coreViewModel.viewDidAppear()
    }

    func viewDidDisappear() {
        dataLoading = false
        wasAnswered = false
        coreViewModel.viewDidDisappear()
    }

    @MainActor
    func onFinish() {
        if wasAnswered {
            coreViewModel.finish()
        } else {
            coreViewModel.cancel()
        }
        shouldClose = true
    }

    private func extractPathAndParameters(url: URL) -> (String, [String: String]) {
        let lastPathPart = url.lastPathComponent
        var queryPairs: [String: String] = [:]

        URLComponents(url: url, resolvingAgainstBaseURL: false)?.queryItems?.forEach {
            queryPairs[$0.name] = $0.value
        }

        return (lastPathPart, queryPairs)
    }
}

extension LimeSurveyViewModel: WebViewListener {
    func onRedirect(navigationAction: WKNavigationAction) async -> WKNavigationActionPolicy {
        if let url = await navigationAction.request.url {
            print("onRedirect URL: \(url)")
            let (endPath, parameters) = extractPathAndParameters(url: url)
            if endPath.lowercased().contains("end.htm"), parameters.keys.contains("savedid") {
                Task { @MainActor in
                    self.wasAnswered = true
                    self.onFinish()
                }
            }
        }
        return .allow
    }
}

