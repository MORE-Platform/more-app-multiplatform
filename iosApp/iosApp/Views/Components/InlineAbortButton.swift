//
//  InlineButton.swift
//  iosApp
//
//  Created by Isabella Aigner on 20.03.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
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
                Text(String.localizedString(forKey: "Abort", inTable: stringTable, withComment: "Abort running task."))
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
