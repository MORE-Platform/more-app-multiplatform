//
//  MoreNavigationView.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 13.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI

struct MoreNavigationView<Content: View>: View {
    
    var content: () -> Content
    
    var body: some View {
        ZStack {
            if #available(iOS 16.0, *) {
                NavigationStack {
                    content()
                }.accentColor(Color.more.main)
            } else {
                NavigationView {
                    content()
                }.accentColor(Color.more.main)
            }
        }.background(Color.more.mainBackground)
    }
}
