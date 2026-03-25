//
//  WebViewViewModel.swift
//  More
//
//  Created by Jan Cortiel on 15.05.23.
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
import WebKit

protocol WebViewListener {
    func onRedirect(navigationAction: WKNavigationAction) async -> WKNavigationActionPolicy
}

class WebViewViewModel: NSObject, ObservableObject {
    private static let webViewProgressObserverKey = "estimatedProgress"
    let webView = WKWebView(frame: .zero)

    var delegate: WebViewListener?

    @Published var progress: Float = 0

    override init() {
        super.init()
        webView.navigationDelegate = self
        webView.addObserver(self, forKeyPath: WebViewViewModel.webViewProgressObserverKey, options: .new, context: nil)
    }

    override func observeValue(forKeyPath keyPath: String?, of object: Any?, change: [NSKeyValueChangeKey: Any]?, context: UnsafeMutableRawPointer?) {
        if keyPath == WebViewViewModel.webViewProgressObserverKey {
            DispatchQueue.main.async {
                self.progress = Float(self.webView.estimatedProgress)
            }
        }
    }
}

extension WebViewViewModel: WKNavigationDelegate {
    func webView(_ webView: WKWebView, didCommit navigation: WKNavigation!) {
        Napier.d("WebView did commit...")
    }

    func webView(_ webView: WKWebView, didFinish navigation: WKNavigation!) {
        Napier.d("WebView didFinish")
    }

    func webView(_ webView: WKWebView, didFail navigation: WKNavigation!, withError error: Error) {
        Napier.e("WebView didFail with error: \(error)")
    }

    func webView(_ webView: WKWebView, didStartProvisionalNavigation navigation: WKNavigation!) {
        Napier.d("WebView didStartProviisonalNavigation")
    }

    func webView(_ webView: WKWebView, navigationAction: WKNavigationAction, didBecome download: WKDownload) {
        Napier.d("WebView didBecome download")
    }

    func webView(_ webView: WKWebView, didFailProvisionalNavigation navigation: WKNavigation!, withError error: Error) {
        Napier.e("WebView didFail Provisional Navigation with error: \(error)")
    }

    func webView(_ webView: WKWebView, didReceiveServerRedirectForProvisionalNavigation navigation: WKNavigation!) {
        Napier.d("WebView did receive Server redirect for provisional navigation")
    }

    func webView(_ webView: WKWebView, decidePolicyFor navigationAction: WKNavigationAction) async -> WKNavigationActionPolicy {
        if let delegate = delegate {
            return await delegate.onRedirect(navigationAction: navigationAction)
        }
        return .allow
    }
}
