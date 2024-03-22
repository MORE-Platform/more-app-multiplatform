//
//  ObservationDetailsView.swift
//  More
//
//  Created by Isabella Aigner on 19.04.23.
//  Copyright © 2023 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung 
//  Licensed under the Apache 2.0 license with Commons Clause 
//  (see https://www.apache.org/licenses/LICENSE-2.0 and
//  https://commonsclause.com/).
//

import shared
import SwiftUI

struct ObservationDetailsView: View {
    @StateObject var viewModel: ObservationDetailsViewModel
    private let stringTable = "ObservationDetails"
    private let navigationStrings = "Navigation"
    
    var body: some View {
        VStack(
            spacing: 20
        ) {
            VStack(alignment: HorizontalAlignment.leading) {
                HStack {
                    Title2(titleText: viewModel.observationDetailModel?.observationTitle ?? "")
                        .padding(0.5)
                    
                }
                .frame(height: 40)
                HStack(
                ) {
                    BasicText(text: viewModel.observationDetailModel?.observationType ?? "", color: .more.secondary)
                    Spacer()
                }
            }
            
            
            let date: String =
            (viewModel.observationDetailModel?.start.toDateString(dateFormat: "dd.MM.yyyy") ?? "") == (viewModel.observationDetailModel?.end.toDateString(dateFormat: "dd.MM.yyyy") ?? "") ? (viewModel.observationDetailModel?.start.toDateString(dateFormat: "dd.MM.yyyy") ?? "") : (viewModel.observationDetailModel?.start.toDateString(dateFormat: "dd.MM.yyyy") ?? "") + " - " + (viewModel.observationDetailModel?.end.toDateString(dateFormat: "dd.MM.yyyy") ?? "")
            
            let time: String = (viewModel.observationDetailModel?.start.toDateString(dateFormat: "HH:mm") ?? "") + " - " + (viewModel.observationDetailModel?.end.toDateString(dateFormat: "HH:mm") ?? "")
            
            ObservationDetailsData(dateRange: date, timeframe: time)
            
            HStack {
                AccordionItem(title: String.localize(forKey: "Participant Information", withComment: "Participant Information of specific task.", inTable: stringTable), info: viewModel.observationDetailModel?.participantInformation ?? "", isOpen: true)
            }
            .padding(.top, 10)
            
            Spacer()
        }
        .customNavigationTitle(with: NavigationScreen.observationDetails.localize(useTable: navigationStrings, withComment: "Observation Detail"))
        .onAppear {
            viewModel.viewDidAppear()
        }
        .onDisappear {
            viewModel.viewDidDisappear()
        }
    }
}
