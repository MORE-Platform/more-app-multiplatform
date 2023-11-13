//
//  ListView.swift
//  iosApp
//
//  Created by Jan Cortiel on 15.03.23.
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


struct ClearListStyleModifier: ViewModifier {
    func body(content: Content) -> some View {
        if #available(iOS 16, *) {
            content.scrollContentBackground(.hidden)
        } else {
            content
        }
    }
}

struct ListRowModifier: ViewModifier {
    func body(content: Content) -> some View {
        if #available(iOS 16, *) {
            content
                .listRowSeparator(.hidden)
        } else {
            content
        }
    }
}

extension View {
    @ViewBuilder
    func clearListBackground() -> some View {
        self.modifier(ClearListStyleModifier())
    }
    
    @ViewBuilder
    func hideListRowSeparator() -> some View {
        self.modifier(ListRowModifier())
    }
}
