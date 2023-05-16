//
//  LimeSurveyViewModel.swift
//  More
//
//  Created by Jan Cortiel on 10.05.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import Foundation
import shared
import WebKit

class LimeSurveyViewModel: ObservableObject {
    private let coreViewModel = CoreLimeSurveyViewModel(observationFactory: AppDelegate.observationFactory)

    let webViewModel = WebViewViewModel()

    @Published var limeSurveyLink: URL?
    @Published var dataLoading = false
    @Published var wasAnswered = false

    init(scheduleId: String) {
        webViewModel.delegate = self
        coreViewModel.setScheduleId(scheduleId: scheduleId)
        coreViewModel.onLimeSurveyLinkChange { [weak self] link in
            print("Link: \(String(describing: link))")
            DispatchQueue.main.async {
                if let link {
                    self?.limeSurveyLink = URL(string: link)
                } else {
                    self?.limeSurveyLink = nil
                }
            }
        }
        coreViewModel.onDataLoadingChange { [weak self] boolean in
            DispatchQueue.main.async {
                self?.dataLoading = boolean.boolValue
            }
        }
    }

    func viewDidAppear() {
        coreViewModel.viewDidAppear()
    }

    func viewDidDisappear() {
        coreViewModel.viewDidDisappear()
    }

    func onFinish() {
        if wasAnswered {
            coreViewModel.finish()
        } else {
            coreViewModel.cancel()
        }
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
                }
            }
        }
        return .allow
    }
}
