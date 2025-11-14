//
//  GarminConnectViewModel.swift
//  More
//
//  Created by Jan Cortiel on 13.11.25.
//  Copyright © 2025 Redlink GmbH. All rights reserved.
//

import Foundation
import shared
import KMPNativeCoroutinesCombine
import Combine
import WebKit

class GarminConnectViewModel: ObservableObject {
    let coreViewModel = CoreGarminConnectViewModel(networkService: AppDelegate.shared.networkService, sharedStorageRepository: AppDelegate.shared.sharedStorageRepository)
    let webViewModel = WebViewViewModel()

    @Published var isLoading = false
    @Published var shouldClose = false
    private var didHandleCallback = false
    private var allowedHost: String? = nil
    private var injectedURLs: Set<String> = []

    private var cancellables: Set<AnyCancellable> = []

    init() {
        webViewModel.delegate = self
        createPublisher(for: coreViewModel.isLoading)
        .receive(on: DispatchQueue.main)
        .sink { _ in
        } receiveValue: { [weak self] isLoading in
            self?.isLoading = isLoading.boolValue
        }
        .store(in: &cancellables)
    }

    func getUrl() -> URLRequest? {
        if let url = coreViewModel.garminSSOUrl()?.description(), let requestUrl = URL(string: url) {
            print(url)
            var request = URLRequest(url: requestUrl)

            allowedHost = requestUrl.host

            request.setValue(coreViewModel.basicAuthHeader(forUrl: url), forHTTPHeaderField: "Authorization")
            return request
        }
        return nil
    }

    func handleCallbackIfNeeded(_ url: URL) {
        guard !didHandleCallback,
              coreViewModel.checkIfUrlIsCallback(url: url.absoluteString)
        else {
            return
        }

        didHandleCallback = true
        coreViewModel.setLoading(state: false)

        var failure = true
        Task {
            do {
                if try await coreViewModel.sendCallback(url: url.absoluteString).boolValue {
                    failure = false
                    await MainActor.run { [weak self] in
                        self?.coreViewModel.onSuccess()
                        self?.shouldClose = true
                    }
                }
            } catch {
                print("Exception during Callback \(error)")
                didHandleCallback = false
            }
            if failure {
                await MainActor.run {
                    let dialog = AlertDialogModel(title: "Error during Callback", message: "Error accessing your Garmin Connect Account! Please try again later!", confirmLabel: "Ok", cancelLabel: nil, onConfirm: { [weak self] in
                        self?.coreViewModel.closeView()
                        self?.shouldClose = true
                    })
                    AlertController.shared.openAlertDialog(model: dialog)
                }
            }
        }
    }


}

extension GarminConnectViewModel: WebViewListener {
    func onRedirect(navigationAction: WKNavigationAction) async -> WKNavigationActionPolicy {
        guard await navigationAction.targetFrame?.isMainFrame == true,
              let url = await navigationAction.request.url
        else {
            return .allow
        }


        if coreViewModel.checkIfUrlIsCallback(url: url.absoluteString) {
            await MainActor.run {
                self.handleCallbackIfNeeded(url)
            }
            return .cancel
        }


        if let allowedHost, let host = url.host, !host.hasSuffix(allowedHost) {
            return .allow
        }


        let urlKey = url.absoluteString
        if injectedURLs.contains(urlKey) {
            injectedURLs.remove(urlKey)
            return .allow
        }


        if await navigationAction.request.value(forHTTPHeaderField: "Authorization") != nil {
            return .allow
        }


        guard let authHeader = coreViewModel.basicAuthHeader(forUrl: url.absoluteString) else {
            return .allow
        }


        var request = await navigationAction.request

        request.url = url

        if request.httpMethod == nil {
            request.httpMethod = "GET"
        }
        request.setValue(authHeader, forHTTPHeaderField: "Authorization")


        injectedURLs.insert(urlKey)

        await webViewModel.webView.load(request)
        return .cancel
    }
}

