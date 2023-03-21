//
//  TaskDetailsView.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 20.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI
import shared

struct TaskDetailsView: View {
    
    @StateObject var viewModel: TaskDetailsViewModel
    
    var body: some View {
        Text(viewModel.taskDetailsModel?.observationTitle ?? "")
    }
}

