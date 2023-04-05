//
//  TaskProgressView.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 07.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI

struct TaskProgressView: View {
    
    @Binding var progressViewTitle: String
    @State var totalTasks: Double
    @State var tasksCompleted: Double
    
    var body: some View {
        VStack {
            HStack {
                BasicText(text: $progressViewTitle, color: Color.more.secondary)
                Spacer()
                BasicText(text: .constant(String(format: "%d%%", (tasksCompleted / totalTasks) * 100)))
            }.foregroundColor(Color.more.secondary)
            ProgressView(value: tasksCompleted, total: totalTasks)
                .progressViewStyle(LinearProgressViewStyle())
                .foregroundColor(Color.more.secondaryMedium)
                .accentColor(Color.more.primary)
                .scaleEffect(x: 1, y: 5)
                .padding(.bottom)
        }
    }
}

struct TaskProgressView_Previews: PreviewProvider {
    static var previews: some View {
        TaskProgressView(progressViewTitle: .constant("Progress Bar Title"), totalTasks: 10, tasksCompleted: 5)
    }
}
