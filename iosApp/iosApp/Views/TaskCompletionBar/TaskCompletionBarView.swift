//
//  TaskCompletionBarView.swift
//  More
//
//  Created by Julia Mayrhauser on 26.04.23.
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
import shared

struct TaskCompletionBarView: View {
    
    @StateObject var viewModel: TaskCompletionBarViewModel
    var progressViewTitle: String = ""

    
    var body: some View {
        VStack {
            HStack {
                if progressViewTitle != "" {
                    BasicText(text: progressViewTitle, color: Color.more.secondary)
                }
                
                Spacer()
                if viewModel.taskCompletion.totalTasks != 0 {
                    BasicText(text: String(format: "%.2f%%", viewModel.taskCompletionPercentage))
                }
            }.foregroundColor(Color.more.secondary)
            ProgressView(value: Double(viewModel.taskCompletion.finishedTasks), total: Double(viewModel.taskCompletion.totalTasks))
                .progressViewStyle(LinearProgressViewStyle())
                .foregroundColor(Color.more.secondaryMedium)
                .accentColor(Color.more.primary)
                .scaleEffect(x: 1, y: 5)
                .padding(.bottom)
        }
        .onAppear {
            viewModel.loadTaskCompletion()
        }
    }
}

struct TaskCompletionBarView_Previews: PreviewProvider {
    static var previews: some View {
        TaskCompletionBarView(viewModel: TaskCompletionBarViewModel(), progressViewTitle: "Progress Bar Title")
    }
}
