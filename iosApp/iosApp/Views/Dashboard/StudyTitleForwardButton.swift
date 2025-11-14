//
//  StudyTitleForwardButton.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 07.03.23.
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

struct StudyTitleForwardButton: View {
    var title: String
    let action: () -> Void = {}
    
    var body: some View {
        Button {
            action()
        } label: {
            HStack {
                Title(titleText: title)
                Spacer()
                Image(systemName: "chevron.forward")
            }
        }
    }
}

struct StudyTitleForwardButton_Previews: PreviewProvider {
    static var previews: some View {
        StudyTitleForwardButton(title: "Study Title")
    }
}
