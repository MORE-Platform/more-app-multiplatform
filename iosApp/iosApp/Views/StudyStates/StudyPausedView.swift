//
//  StudyPausedView.swift
//  More
//
//  Created by Jan Cortiel on 25.07.23.
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

struct StudyPausedView: View {
    private let stringsTable = "StudyStates"
    var body: some View {
        MoreMainBackgroundView {
            VStack(alignment: .center) {
                Spacer()
                Title(titleText: "\("Study currently paused".localize(withComment: "Study paused", useTable: stringsTable))!", textAlignment: .center)
                    .padding(.bottom, 8)
                Title2(titleText: "\("This study is currently paused by the Study Operator and will be resumed shortly".localize(withComment: "Study will be resumed shortly", useTable: stringsTable))!", textAlignment: .center)
                Spacer()
            }
        }
    }
}

struct StudyPausedView_Previews: PreviewProvider {
    static var previews: some View {
        StudyPausedView()
    }
}
