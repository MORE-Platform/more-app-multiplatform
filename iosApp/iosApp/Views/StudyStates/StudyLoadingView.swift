//
//  StudyLoadingView.swift
//  More
//
//  Created by Jan Cortiel on 17.09.25.
//  Copyright © 2025 Redlink GmbH. All rights reserved.
//  Copyright © 2024 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung
//  Licensed under the Apache 2.0 license
//  (see https://www.apache.org/licenses/LICENSE-2.0).
//

import SwiftUI

struct StudyLoadingView: View {
    var body: some View {
        VStack(alignment: .center) {
            Spacer()
            Title(titleText: "Study loading…", textAlignment: .center)
            ProgressView()
                .tint(.more.primary)
                .scaleEffect(1.5)
                .padding(.vertical, 8)
            Spacer()
        }
    }
}

#Preview {
    StudyLoadingView()
}
