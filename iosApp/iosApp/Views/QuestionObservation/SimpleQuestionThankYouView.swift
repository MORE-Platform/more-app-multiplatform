//
//  SimpleQuestionThankYouView.swift
//  More
//
//  Created by Julia Mayrhauser on 27.04.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import SwiftUI

struct SimpleQuestionThankYouView: View {
    @EnvironmentObject var dashboardViewModel: DashboardViewModel
    var body: some View {
        VStack {
            Title(titleText: .constant("Thank You!"))
            BasicText(text: .constant("Your answer to the question has been successfully submitted!"))
                .padding(.bottom, 8)
            BasicText(text: .constant("Thank You for your participation"))
            Spacer()
            NavigationLinkButton(disabled: .constant(true), destination: {
                DashboardView(dashboardViewModel: dashboardViewModel)
            }, label: {
                BasicText(text: .constant("Return to dashboard"))
            })
        }
    }
}
