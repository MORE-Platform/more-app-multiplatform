//
//  SwiftUIWebView.swift
//  More
//
//  Created by Jan Cortiel on 11.05.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import SwiftUI
import WebKit

struct WebView: View {
    @State var url: URL?
    @StateObject var viewModel: WebViewViewModel
    
    var body: some View {
        VStack {
            if viewModel.progress < 1 {
                ProgressView(value: viewModel.progress, total: 1)                
            }
            SwiftUIWebView(viewModel: viewModel, url: url)
        }
    }
}

struct SwiftUIWebView: UIViewRepresentable {
    typealias UIViewType = WKWebView
    
    private let url: URL?
    private let viewModel: WebViewViewModel
    
    init(viewModel: WebViewViewModel, url: URL?) {
        self.viewModel = viewModel
        self.url = url
    }
    
    func makeUIView(context: Context) -> WKWebView {
        self.viewModel.webView
    }
    
    func updateUIView(_ uiView: WKWebView, context: Context) {
        print("WebView URL: \(String(describing: url))")
        if let url {
            self.viewModel.webView.load(URLRequest(url: url))
        }
    }
}

struct SwiftUIWebView_Previews: PreviewProvider {
    static var previews: some View {
        WebView(url: URL(string: "https://www.devtechie.com")!, viewModel: WebViewViewModel())
    }
}
