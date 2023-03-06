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
    private let stringTable = "DashboardView"
    @State private var totalTasks: Double = 0
    @State private var selection: Int = 0
    @State private var tasksCompleted: Double = 0
    var body: some View {
        VStack {
            Button {
                
            } label: {
                HStack {
                    Title(titleText: $viewModel.studyTitle)
                    Spacer()
                    Button {
                        
                    } label: {
                        Image(systemName: "chevron.forward")
                    }
                }
                .foregroundColor(Color.more.main)
                .padding(.bottom)
            }
            Picker ("", selection: $selection){
                BasicText(text: .constant(String
                    .localizedString(forKey: "schedule_string", inTable: stringTable, withComment: "schedule tab is selected")))
                    .tag(0)
                BasicText(text: .constant(String
                    .localizedString(forKey: "modules_string", inTable: stringTable, withComment: "modules tab is selected"))).tag(1)
            }.pickerStyle(.segmented)
                .frame(height: 50)
                .colorMultiply(Color.more.mainLight)
                .padding(.bottom)
            Button {
                
            } label: {
                HStack {
                    BasicText(text: .constant(String
                        .localizedString(forKey: "no_filter_activated", inTable: stringTable, withComment: "string if no filter is selected")))
                    Image(systemName: "slider.horizontal.3")
                        .foregroundColor(Color.more.icons)
                }.padding(.bottom)
            }
            HStack {
                BasicText(text: .constant(String
                    .localizedString(forKey: "tasks_completed", inTable: stringTable,
                                     withComment: "string for completed tasks")))
                .foregroundColor(Color.more.icons)
                Spacer()
                BasicText(text: .constant(String(format: "%d%%", tasksCompleted / totalTasks)))
                    .foregroundColor(Color.more.main)
            }.foregroundColor(Color.more.icons)
            
            ProgressView(value: tasksCompleted, total: totalTasks)
                .progressViewStyle(LinearProgressViewStyle())
                .foregroundColor(Color.more.inactiveText)
                .accentColor(Color.more.main)
                .scaleEffect(x: 1, y: 5)
                .padding(.bottom)
            Divider()
            ScheduleList(model: viewModel)
        }
    }
}

struct DashboardView_Previews: PreviewProvider {
    static var previews: some View {
        MoreMainBackgroundView {
            DashboardView(viewModel: DashboardViewModel())
        } topBarContent: {
            HStack {
                Button {
                } label: {
                    Image(systemName: "bell.fill")
                }
                Button {
                    
                } label: {
                    Image(systemName: "gearshape.fill")
                }
            }.foregroundColor(Color.more.icons)
        }
    }
}

