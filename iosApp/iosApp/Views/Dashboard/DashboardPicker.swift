//
//  DashboardPicker.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 07.03.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import SwiftUI

struct DashboardPicker: View {
    @Binding var selection: Int
    private let stringTable = "DashboardView"
    var firstTab: String
    var secondTab: String
    var body: some View {
        Picker ("", selection: $selection){
            BasicText(text: firstTab)
                .tag(0)
            BasicText(text: secondTab).tag(1)
        }.pickerStyle(.segmented)
            .frame(height: 50)
            .colorMultiply(Color.more.primaryLight)
    }
}

struct DashboardPicker_Previews: PreviewProvider {
    static var previews: some View {
        DashboardPicker(selection: .constant(0), firstTab: "tab 1", secondTab: "tab 2")
    }
}
