//
//  MoreBackButton.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 13.03.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import SwiftUI

@available(iOS 15.0, *)
struct MoreBackButton: View {
    @Environment(\.dismiss) private var dismiss
    var action: () -> Void = {}
    var body: some View {
        Button {
            action()
            dismiss()
        } label: {
            Image(systemName: "chevron.left")
        }
    }
}

struct MoreBackButtonIOS14: View {
    var action: () -> Void = {}
    @State private var isActive: Bool = false
    var body: some View {
        HStack {
            Button {
                isActive = true
            } label: {
                Image(systemName: "chevron.left")
            }
        }
    }
}
