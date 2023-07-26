//
//  StudyUpdateView.swift
//  More
//
//  Created by Jan Cortiel on 25.07.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import SwiftUI

struct StudyUpdateView: View {
    
    private let stringTable = "StudyStates"
    var body: some View {
        MoreMainBackgroundView {
            VStack(alignment: .center) {
                Spacer()
                Title(titleText: "The study configuration is currently updating".localize(withComment: "Study is currently updating", useTable: stringTable), textAlignment: .center)
                    .padding(.bottom, 8)
                Title2(titleText: "Please wait until this process is finished".localize(withComment: "Please wait until this process finishes", useTable: stringTable), textAlignment: .center)
                ProgressView()
                    .scaleEffect(1.5)
                    .padding(.vertical, 8)
                Spacer()
            }
        }
    }
}

struct StudyUpdateView_Previews: PreviewProvider {
    static var previews: some View {
        StudyUpdateView()
    }
}
