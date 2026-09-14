//
//  ReloadButton.swift
//  BlendedCare
//
//  Created by Jan Cortiel on 27.01.26.
//  Copyright © 2026 Redlink GmbH. All rights reserved.
//  Copyright © 2024 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung
//  Licensed under the Apache 2.0 license
//  (see https://www.apache.org/licenses/LICENSE-2.0).
//

import SwiftUI
import shared

struct ReloadButton: View {
    @EnvironmentObject private var navigationModalState: NavigationModalState
    @State private var isLoading = false
    var body: some View {
        MoreActionButton(backgroundColor: .more.primary, disabled: $isLoading) {
            reload()
        } label: {
            Text("Reload study")
        }
    }
    
    private func reload() {
        isLoading = true
        AppDelegate.shared.updateStudy(oldStudyState: nil, newStudyState: nil)
        isLoading = false
    }
}

#Preview {
    ReloadButton()
}
