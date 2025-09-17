//
//  StudyUpdateView.swift
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

struct StudyUpdateView: View {
    var body: some View {
        VStack(alignment: .center) {
            Spacer()
            Title(titleText: "The study configuration is currently updating", textAlignment: .center)
                .padding(.bottom, 8)
            Title2(titleText: "Please wait until this process is finished", textAlignment: .center)
            ProgressView()
                .scaleEffect(1.5)
                .padding(.vertical, 8)
            Spacer()
        }
    }
}

struct StudyUpdateView_Previews: PreviewProvider {
    static var previews: some View {
        StudyUpdateView()
    }
}
