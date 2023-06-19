//
//  MoreNavigationView.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 13.03.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import SwiftUI

struct Navigation<Content: View>: View {
    var content: () -> Content
    var body: some View {
        ZStack {
            if #available(iOS 16.0, *) {
                NavigationStack {
                    content()
                }
                .tint(.more.primary)
            } else {
                NavigationView {
                    content()
                }
                .navigationViewStyle(.stack)
                .accentColor(.more.primary)
            }
        }.background(Color.more.secondaryLight)
    }
}

@available(iOS 16, *)
struct NavigationTitleViewModifier: ViewModifier {
    var text: String
    var displayMode: NavigationBarItem.TitleDisplayMode = .automatic
    
    func body(content: Content) -> some View {
        content
            .navigationTitle(text)
            .navigationBarTitleDisplayMode(displayMode)
    }
}

struct NavigationBarTitleViewModifier: ViewModifier {
    var text: String
    var displayMode: NavigationBarItem.TitleDisplayMode = .automatic
    
    func body(content: Content) -> some View {
        content
            .navigationBarTitle(text, displayMode: displayMode)
    }
}

extension View {
    @ViewBuilder
    func customNavigationTitle(with text: String, displayMode: NavigationBarItem.TitleDisplayMode = .inline) -> some View {
        if #available(iOS 16, *) {
            self.modifier(NavigationTitleViewModifier(text: text, displayMode: displayMode))
        }
        else {
            self.modifier(NavigationBarTitleViewModifier(text: text, displayMode: displayMode))
        }
    }
    
    @ViewBuilder
    func textFieldAutoCapitalizataion(uppercase: Bool) -> some View {
        if #available(iOS 15, *) {
            self.modifier(TextFieldViewModifier(uppercase: uppercase))
        } else {
            self.modifier(TextFieldOldViewModifier(uppercase: uppercase))
        }
    }
}

@available(iOS 15, *)
struct PresentationViewModifier: ViewModifier {
    func body(content: Content) -> some View {
        content
            .interactiveDismissDisabled()
    }
}

struct PresentationCoverViewModifier: ViewModifier {
    func body(content: Content) -> some View {
        content
    }
}

@available(iOS 15, *)
struct TextFieldViewModifier: ViewModifier {
    let uppercase: Bool
    func body(content: Content) -> some View {
        content.textInputAutocapitalization(uppercase ? .characters : .none)
    }
}

struct TextFieldOldViewModifier: ViewModifier {
    let uppercase: Bool
    func body(content: Content) -> some View {
        content.autocapitalization(uppercase ? .allCharacters : .none)
    }
}
