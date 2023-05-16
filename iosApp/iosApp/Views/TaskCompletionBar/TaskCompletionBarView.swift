//
//  TaskCompletionBarView.swift
//  More
//
//  Created by Julia Mayrhauser on 26.04.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import SwiftUI
import shared

struct TaskCompletionBarView: View {
    
    @StateObject var viewModel: TaskCompletionBarViewModel
    @Binding var progressViewTitle: String
    
    var body: some View {
        VStack {
            HStack {
                BasicText(text: $progressViewTitle, color: Color.more.secondary)
                Spacer()
                if viewModel.taskCompletion.totalTasks != 0 {
                    BasicText(text: .constant(String(format: "%.2f%%", viewModel.taskCompletionPercentage)))
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
        TaskCompletionBarView(viewModel: TaskCompletionBarViewModel(), progressViewTitle: .constant("Progress Bar Title"))
    }
}
