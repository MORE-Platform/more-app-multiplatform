//
//  ObservationDetailsView.swift
//  More
//
//  Created by Isabella Aigner on 19.04.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import shared
import SwiftUI

struct ObservationDetailsView: View {
    @StateObject var viewModel: ObservationDetailsViewModel
    private let stringTable = "ObservationDetail"
    private let navigationStrings = "Navigation"
    
    var body: some View {
        Navigation {
            MoreMainBackgroundView {
                VStack(
                    spacing: 20
                ) {
                    VStack {
                        HStack {
                            Title2(titleText: .constant(viewModel.observationDetailModel?.observationTitle ?? ""))
                                .padding(0.5)
                            // abort button
                        }
                        .frame(height: 40)
                        HStack(
                        ) {
                            BasicText(text: .constant(viewModel.observationDetailModel?.observationType ?? ""), color: .more.secondary)
                            Spacer()
                        }
                    }

                    let date: String =
                        (viewModel.observationDetailModel?.start.toDateString(dateFormat: "dd.MM.yyyy") ?? "") == (viewModel.observationDetailModel?.end.toDateString(dateFormat: "dd.MM.yyyy") ?? "") ? (viewModel.observationDetailModel?.start.toDateString(dateFormat: "dd.MM.yyyy") ?? "") : (viewModel.observationDetailModel?.start.toDateString(dateFormat: "dd.MM.yyyy") ?? "") + " - " + (viewModel.observationDetailModel?.end.toDateString(dateFormat: "dd.MM.yyyy") ?? "")
                    
                    let time: String = (viewModel.observationDetailModel?.start.toDateString(dateFormat: "HH:mm") ?? "") + " - " + (viewModel.observationDetailModel?.end.toDateString(dateFormat: "HH:mm") ?? "")
                    
                    ObservationDetailsData(dateRange: .constant(date), timeframe: .constant(time))
                    
                    HStack {
                        AccordionItem(title: String.localizedString(forKey: "Participant Information", inTable: stringTable, withComment: "Participant Information of specific task."), info: .constant(viewModel.observationDetailModel?.participantInformation ?? ""), isOpen: true)
                    }
                    .padding(.top, 10)
                    
                    Spacer()
                }

            } topBarContent: {
                EmptyView()
            }
            .customNavigationTitle(with: NavigationScreens.observationDetails.localize(useTable: navigationStrings, withComment: "Observation Detail"))
            .navigationBarTitleDisplayMode(.inline)
        }
    }
}
