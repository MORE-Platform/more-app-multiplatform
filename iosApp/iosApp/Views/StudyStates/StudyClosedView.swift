//
//  StudyClosedView.swift
//  More
//
//  Created by Jan Cortiel on 25.07.23.
//  Copyright © 2023 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung
//  Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
//

import SwiftUI

struct StudyClosedView: View {
    var body: some View {
        VStack {
            ScrollView {
                VStack(alignment: .center) {
                    Title(titleText: "Study closed")
                        .padding(.bottom, 8)
                    Title2(titleText: "\("Thank you for your participation")!")
                }
                if let finishText = AppDelegate.shared.repositories.study.finishTextValue {
                    Divider()
                    VStack {
                        SectionHeading(sectionTitle: "\("Message by the Study Operator"):")
                            .padding(.vertical, 8)
                        BasicText(text: finishText)
                    }
                }
            }
            MoreActionButton(disabled: .constant(false)) {
                AppDelegate.shared.exitStudy(onComplete: {})
            } label: {
                BasicText(text: "Leave Study", color: .more.white, font: .headline)
            }
        }
        .padding(.vertical)
    }
}

struct StudyClosedView_Previews: PreviewProvider {
    static var previews: some View {
        StudyClosedView()
    }
}
