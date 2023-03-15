//
//  ListView.swift
//  iosApp
//
//  Created by Jan Cortiel on 15.03.23.
//  Copyright © 2023 orgName. All rights reserved.
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
