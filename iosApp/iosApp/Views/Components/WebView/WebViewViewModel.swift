//
//  WebViewViewModel.swift
//  More
//
//  Created by Jan Cortiel on 15.05.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import Foundation
import WebKit

protocol WebViewListener {
    func onRedirect(navigationAction: WKNavigationAction) -> WKNavigationActionPolicy
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
        print("WebView did commit...")
    }

    func webView(_ webView: WKWebView, didFinish navigation: WKNavigation!) {
        print("WebView didFinish")
    }

    func webView(_ webView: WKWebView, didFail navigation: WKNavigation!, withError error: Error) {
        print("WebView didFail with error: \(error)")
    }

    func webView(_ webView: WKWebView, didStartProvisionalNavigation navigation: WKNavigation!) {
        print("WebView didStartProviisonalNavigation")
    }

    @available(iOS 14.5, *)
    func webView(_ webView: WKWebView, navigationAction: WKNavigationAction, didBecome download: WKDownload) {
        print("WebView didBecome download")
    }

    func webView(_ webView: WKWebView, didFailProvisionalNavigation navigation: WKNavigation!, withError error: Error) {
        print("WebView didFail Provisional Navigation with error: \(error)")
    }

    func webView(_ webView: WKWebView, didReceiveServerRedirectForProvisionalNavigation navigation: WKNavigation!) {
        print("WebView did receive Server redirect for provisional navigation")
    }

    func webView(_ webView: WKWebView, decidePolicyFor navigationAction: WKNavigationAction) async -> WKNavigationActionPolicy {
        return delegate?.onRedirect(navigationAction: navigationAction) ?? .allow
    }
}
