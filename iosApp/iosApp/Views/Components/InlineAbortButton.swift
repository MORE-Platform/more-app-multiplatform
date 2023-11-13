//
//  InlineButton.swift
//  iosApp
//
//  Created by Isabella Aigner on 20.03.23.
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

struct InlineAbortButton: View {
    private let stringTable = "TaskDetail"
    var action: () -> Void = {}
    var body: some View {
        Button {
            action()
        } label: {
            HStack{
                Image(systemName: "square.fill")
                    .padding(0.5)
                    
                    .foregroundColor(.more.important)
                Text(String.localize(forKey: "Abort", withComment: "Abort running task.", inTable: stringTable))
                    .foregroundColor(.more.secondary)
            }
            .padding(5)
            
        }
        .accent(color: .more.primaryLight)
        .overlay(
            RoundedRectangle(cornerRadius: 4)
                .stroke(Color.more.secondaryMedium, lineWidth: 1)
        )
    }
}
