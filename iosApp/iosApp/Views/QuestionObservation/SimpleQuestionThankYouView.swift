//
//  SimpleQuestionThankYouView.swift
//  More
//
//  Created by Julia Mayrhauser on 27.04.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import SwiftUI

struct SimpleQuestionThankYouView: View {
    @Environment(\.presentationMode) var presentationMode
    @EnvironmentObject var viewModel: SimpleQuestionObservationViewModel
    var body: some View {
        VStack {
            Title(titleText: .constant("Thank You!"))
            BasicText(text: .constant("Your answer to the question has been successfully submitted!"))
                .padding(.bottom, 8)
            BasicText(text: .constant("Thank You for your participation"))
            Spacer()
            MoreActionButton(disabled: .constant(false)) {
                viewModel.questionAnswered()
                presentationMode.wrappedValue.dismiss()
            } label: {
                BasicText(text: .constant("Return to Dashboard"), color: .more.white)
            }
        }.navigationBarBackButtonHidden(true)
    }
}
