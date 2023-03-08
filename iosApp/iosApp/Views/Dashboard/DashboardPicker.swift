//
//  DashboardPicker.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 07.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI

struct DashboardPicker: View {
    @State var selection: Int
    private let stringTable = "DashboardView"
    @Binding var firstTab: String
    @Binding var secondTab: String
    var body: some View {
        Picker ("", selection: $selection){
            BasicText(text: $firstTab)
                .tag(0)
            BasicText(text: $secondTab).tag(1)
        }.pickerStyle(.segmented)
            .frame(height: 50)
            .colorMultiply(Color.more.mainLight)
    }
}

struct DashboardPicker_Previews: PreviewProvider {
    static var previews: some View {
        DashboardPicker(selection: 0, firstTab: .constant("tab 1"), secondTab: .constant("tab 2"))
    }
}
