//
//  ReloadButton.swift
//  BlendedCare
//
//  Created by Jan Cortiel on 27.01.26.
//  Copyright © 2026 Redlink GmbH. All rights reserved.
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
