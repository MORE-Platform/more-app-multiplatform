//
//  MoreBackButton.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 13.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI

@available(iOS 15.0, *)
struct MoreBackButton: View {
    @Environment(\.dismiss) private var dismiss
    @Binding var showContent: Bool
    var body: some View {
        Button {
            showContent = !showContent
            dismiss()
        } label: {
            Image(systemName: "chevron.left")
        }
    }
}

struct MoreBackButtonIOS14: View {
    @Environment(\.presentationMode) var presentationMode
    @Binding var showContent: Bool
    var body: some View {
        Button {
            showContent = !showContent
            presentationMode.wrappedValue.dismiss()
        } label: {
            Image(systemName: "chevron.left")
        }
    }
}
