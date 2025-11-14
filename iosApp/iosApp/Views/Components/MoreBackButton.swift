//
//  MoreBackButton.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 13.03.23.
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
