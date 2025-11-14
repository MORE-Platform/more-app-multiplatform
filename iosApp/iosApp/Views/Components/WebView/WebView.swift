//
//  SwiftUIWebView.swift
//  More
//
//  Created by Jan Cortiel on 11.05.23.
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
        } else {
            self.viewModel.webView.load(URLRequest(url: URL(string:"about:blank")!))
        }
    }
}

struct SwiftUIWebView_Previews: PreviewProvider {
    static var previews: some View {
        WebView(url: URL(string: "https://www.devtechie.com")!, viewModel: WebViewViewModel())
    }
}
