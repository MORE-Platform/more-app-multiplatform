//
//  DashboardView.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 02.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI
import shared

struct DashboardView: View {
    @StateObject var viewModel: DashboardViewModel
    var body: some View {
        BasicText(text: $viewModel.studyTitle)
    }
}

struct DashboardView_Previews: PreviewProvider {
    static var previews: some View {
        DashboardView(viewModel: DashboardViewModel())
    }
}

