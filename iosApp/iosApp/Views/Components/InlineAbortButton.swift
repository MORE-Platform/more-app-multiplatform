//
//  InlineButton.swift
//  iosApp
//
//  Created by Isabella Aigner on 20.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI

@available(iOS 15.0, *)
struct InlineAbortButton: View {
    @Environment(\.dismiss) private var dismiss
    private let defaultStrings = "Default"
    var action: () -> Void = {}
    var body: some View {
        Button {
            action()
            dismiss()
        } label: {
            HStack{
                Image(systemName: "square.fill")
                    .padding(0.5)
                    .foregroundColor(.more.important)
                Text("Abort")
                    .foregroundColor(.more.secondary)
            }
            
        }
        .buttonStyle(.bordered)
        .tint(.more.primaryLight)
        .overlay(
            RoundedRectangle(cornerRadius: 4)
                .stroke(Color.more.secondaryMedium, lineWidth: 1)
        )
    }
}

struct InlineAbortButtonIOS14: View {
    var action: () -> Void = {}
    @State private var isActive: Bool = false
    var body: some View {
        HStack {
            Button {
                isActive = true
            } label: {
                Image(systemName: "square.fill")
                    .padding(0.5)
                    .foregroundColor(.more.important)
                Text("Abort")
            }
            
        }
    }
}
