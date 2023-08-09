//
//  StudyClosedView.swift
//  More
//
//  Created by Jan Cortiel on 25.07.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import SwiftUI

struct StudyClosedView: View {
    @StateObject var viewModel: ContentViewModel
    private let stringsTable = "StudyState"
    var body: some View {
        MoreMainBackgroundView {
            VStack {
                ScrollView {
                    VStack(alignment: .center) {
                        Title(titleText: "\("Study was completed".localize(withComment: "Study closed", useTable: stringsTable))!")
                            .padding(.bottom, 8)
                        Title2(titleText: "\("Thank you for you participation".localize(withComment: "Thanks for the participation", useTable: stringsTable))!")
                    }
                    Divider()
                    VStack {
                        SectionHeading(sectionTitle: "\("Message by the Study Operator".localize(withComment: "Study Operator message", useTable: stringsTable)):")
                            .padding(.vertical, 8)
                        if let finishText = AppDelegate.shared.finishText {
                            BasicText(text: finishText)
                        } else {
                            BasicText(text: "Study was completed".localize(withComment: "Study closed", useTable: stringsTable))
                        }
                    }
                }
                MoreActionButton(disabled: .constant(false)) {
                    AppDelegate.shared.exitStudy {
                        viewModel.showLoginView()
                    }
                } label: {
                    BasicText(text: "Leave Study".localize(withComment: "Leave Study Button Text", useTable: stringsTable), color: .more.white, font: .headline)
                }
            }
            .padding(.vertical)
        }
    }
}

struct StudyClosedView_Previews: PreviewProvider {
    static var previews: some View {
        StudyClosedView(viewModel: ContentViewModel())
    }
}
