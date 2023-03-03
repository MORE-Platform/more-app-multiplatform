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
    var body: some View {
        MoreMainBackgroundView {
            VStack {
                Button {
                    
                } label: {
                    HStack {
                        Text(viewModel.studyTitle)
                            .font(.largeTitle)
                        Spacer()
                        Button {
                            
                        } label: {
                            Image(systemName: "chevron.forward")
                        }
                    }
                    .foregroundColor(Color.more.main)
                    .padding(.bottom)
                }
                Button {
                    
                } label: {
                    HStack {
                        Text(String
                            .localizedString(forKey: "no_filter_activated", inTable: stringTable, withComment: "string if no filter is selected"))
                        Image(systemName: "slider.horizontal.3")
                            .foregroundColor(Color.more.icons)
                    }.padding(.bottom)
                }
                HStack {
                    Text(String
                        .localizedString(forKey: "tasks_completed", inTable: stringTable,
                                         withComment: "string for completed tasks"))
                    .foregroundColor(Color.more.icons)
                    Spacer()
                    Text("5%")
                }.foregroundColor(Color.more.icons)
                
                ProgressView(value: 10, total: 100)
                    .progressViewStyle(LinearProgressViewStyle())
                    .foregroundColor(Color.more.inactiveText)
                    .accentColor(Color.more.main)
                    .scaleEffect(x: 1, y: 5)
                    .padding(.bottom)
                Divider()
                ScheduleList(model: viewModel)
            }
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

struct DashboardView_Previews: PreviewProvider {
    static var previews: some View {
        DashboardView(viewModel: DashboardViewModel())
    }
}

