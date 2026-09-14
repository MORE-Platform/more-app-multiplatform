//
//  Copyright © 2024 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung
//  Licensed under the Apache 2.0 license
//  (see https://www.apache.org/licenses/LICENSE-2.0).
//

import SwiftUI

struct CheckboxField: View {
    let id: String
    let label: String
    let isSelected: Bool
    let callback: (String) -> Void

    init(
        id: String,
        label: String,
        isSelected: Bool = false,
        callback: @escaping (String) -> Void
    ) {
        self.id = id
        self.label = label
        self.isSelected = isSelected
        self.callback = callback
    }

    var body: some View {
        Button(action: {
            withAnimation(.none) {
                self.callback(self.id)
            }
        }) {
            HStack(alignment: .center) {
                Image(systemName: self.isSelected ? "checkmark.square.fill" : "square")
                    .foregroundColor(.more.primary)
                    .animation(nil, value: isSelected)
                BasicText(text: label, color: .more.secondary)
                Spacer()
            }.foregroundColor(.more.primaryLight)
        }
        .foregroundColor(.more.white)
        .padding(.bottom, 7)
        .buttonStyle(.plain)
        .contentShape(Rectangle())
        .transaction { $0.animation = nil }
    }
}

struct CheckboxField_Previews: PreviewProvider {
    static var previews: some View {
        CheckboxField(id: "Test", label: "Test", isSelected: false, callback: { selected in
            print("Toggled item is \(selected)")
        })
    }
}
