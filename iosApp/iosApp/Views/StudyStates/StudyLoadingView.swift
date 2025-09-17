//
//  StudyLoadingView.swift
//  More
//
//  Created by Jan Cortiel on 17.09.25.
//  Copyright © 2025 Redlink GmbH. All rights reserved.
//

import SwiftUI

struct StudyLoadingView: View {
    var body: some View {
        VStack(alignment: .center) {
            Spacer()
            Title(titleText: "Study loading…", textAlignment: .center)
            ProgressView()
                .scaleEffect(1.5)
                .padding(.vertical, 8)
            Spacer()
        }
    }
}

#Preview {
    StudyLoadingView()
}
