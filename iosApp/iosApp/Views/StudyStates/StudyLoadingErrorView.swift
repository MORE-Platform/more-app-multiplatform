//
//  StudyLoadingErrorView.swift
//  More
//
//  Created by Jan Cortiel on 25.09.25.
//  Copyright © 2025 Redlink GmbH. All rights reserved.
//

import SwiftUI

struct StudyLoadingErrorView: View {
    var body: some View {
        VStack(alignment: .center) {
            Spacer()
            Image(systemName: "exclamationmark.triangle.fill")
                .font(.system(size: 60))
                .foregroundColor(Color.more.important)
                .padding()
            Title(titleText: "study_loading_error_title", textAlignment: .center)
                .padding(.bottom, 8)
            Title2(titleText: "study_loading_error_message", textAlignment: .center)
            Spacer()
            
            ReloadButton()
            ExitButton()
        }
    }
}

#Preview {
    StudyLoadingErrorView()
}
