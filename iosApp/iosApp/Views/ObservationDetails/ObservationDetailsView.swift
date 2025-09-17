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
    @StateObject private var viewModel: ObservationDetailsViewModel
    
    init(observationId: String) {
        _viewModel = StateObject(wrappedValue: ObservationDetailsViewModel(observationId: observationId))
    }
    
    
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
                AccordionItem(title: "Participant Information", info: viewModel.observationDetailModel?.participantInformation ?? "", isOpen: true)
            }
            .padding(.top, 10)
            
            Spacer()
        }
        .customNavigationTitle(with: NavigationScreen.observationDetails.localize())
        .onAppear {
            viewModel.viewDidAppear()
        }
        .onDisappear {
            viewModel.viewDidDisappear()
        }
    }
}
