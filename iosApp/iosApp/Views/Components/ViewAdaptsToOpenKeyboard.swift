//
//  AdaptsToOpenKeyboard.swift
//  More
//
//  Created by Isabella Aigner on 28.04.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import Foundation
import SwiftUI
import Combine

class KeyboardResponder: ObservableObject {
    @Published var currentHeight: CGFloat = 0

    var keyboardShow: AnyCancellable?
    var keyboardHide: AnyCancellable?

    init() {
        keyboardShow = NotificationCenter.default.publisher(for: UIResponder.keyboardWillShowNotification)
            .merge(with: NotificationCenter.default.publisher(for: UIResponder.keyboardWillChangeFrameNotification))
            .compactMap { $0.userInfo?[UIResponder.keyboardFrameEndUserInfoKey] as? CGRect }
            .map { $0.height }
            .receive(on: DispatchQueue.main)
            .sink { [weak self] height in
                self?.currentHeight = height
            }

        keyboardHide = NotificationCenter.default.publisher(for: UIResponder.keyboardWillHideNotification)
            .map { _ in CGFloat.zero }
            .receive(on: DispatchQueue.main)
            .sink { [weak self] height in
                self?.currentHeight = height
            }
    }
}


struct ViewAdaptsToOpenKeyboard: ViewModifier {
    @ObservedObject var keyboardResponder = KeyboardResponder()
    var animation: Animation = .easeOut(duration: 0.16)
    
    func body(content: Content) -> some View {
        content
            .padding(.bottom, keyboardResponder.currentHeight)
            .animation(animation, value: keyboardResponder.currentHeight)
    }
}

extension View {
    func viewAdaptsToOpenKeyboard(animation: Animation = .easeOut(duration: 0.16)) -> some View {
        return modifier(ViewAdaptsToOpenKeyboard(animation: animation))
    }
}
