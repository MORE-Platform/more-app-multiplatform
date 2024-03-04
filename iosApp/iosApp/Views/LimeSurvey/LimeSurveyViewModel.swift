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
//  Licensed under the Apache 2.0 license with Commons Clause 
//  (see https://www.apache.org/licenses/LICENSE-2.0 and
//  https://commonsclause.com/).
//

import Foundation
import shared
import WebKit

class LimeSurveyViewModel: ObservableObject {
    private let coreViewModel = CoreLimeSurveyViewModel(observationFactory: AppDelegate.shared.observationFactory)

    let webViewModel = WebViewViewModel()

    @Published var limeSurveyLink: URL?
    @Published var dataLoading = false
    @Published var wasAnswered = false

    private var navigationModalState: NavigationModalState?
    
    private var limeSurveyLinkChange: Ktor_ioCloseable?

    init() {
        webViewModel.delegate = self
        coreViewModel.onDataLoadingChange { [weak self] boolean in
            DispatchQueue.main.async {
                self?.dataLoading = boolean.boolValue
            }
        }
        
    }

    func viewDidAppear() {
        coreViewModel.viewDidAppear()
        
        limeSurveyLinkChange = coreViewModel.onLimeSurveyLinkChange { [weak self] link in
            DispatchQueue.main.async {
                if let link {
                    self?.limeSurveyLink = URL(string: link)
                } else {
                    self?.limeSurveyLink = nil
                }
            }
        }
        
    }

    func viewDidDisappear() {
        limeSurveyLinkChange?.close()
        limeSurveyLinkChange = nil
        dataLoading = false
        wasAnswered = false
        coreViewModel.viewDidDisappear()
    }
    
    func setNavigationModalState(navigationModalState: NavigationModalState) {
        self.navigationModalState = navigationModalState
        if let scheduleId = navigationModalState.navigationState.scheduleId {
            coreViewModel.setScheduleId(scheduleId: scheduleId, notificationId: navigationModalState.navigationState.notificationId)
        } else if let observationId = navigationModalState.navigationState.observationId {
            coreViewModel.setObservationId(observationId: observationId, notificationId: navigationModalState.navigationState.notificationId)
        }
    }

    func onFinish() {
        if wasAnswered {
            coreViewModel.finish()
        } else {
            coreViewModel.cancel()
        }
        self.navigationModalState?.closeView(screen: .limeSurvey)
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
    func onRedirect(navigationAction: WKNavigationAction) -> WKNavigationActionPolicy {
        if let url = navigationAction.request.url {
            print("onRedirect URL: \(url)")
            let (endPath, parameters) = extractPathAndParameters(url: url)
            if endPath.lowercased().contains("end.htm"), parameters.keys.contains("savedid") {
                DispatchQueue.main.async {
                    self.wasAnswered = true
                    self.onFinish()
                }
            }
        }
        return .allow
    }
}
