//
//  ConsentView.swift
//  iosApp
//
//  Created by Jan Cortiel on 08.02.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI
import shared

struct ConsentView: View {
    @EnvironmentObject var contentViewModel: ContentViewModel
    @StateObject var viewModel: ConsentViewModel
    
    var body: some View {
        MoreMainBackgroundView {
            VStack() {
                VStack(alignment: .leading) {
                    Title(titleText: .constant(viewModel.permissionModel.studyTitle))
                    Divider()
                    BasicText(text: .constant(viewModel.permissionModel.studyParticipantInfo))
                }
                
                
                ConsentList(permissionModel: .constant(viewModel.permissionModel))
                Spacer()
                if viewModel.isLoading {
                    ProgressView()
                        .progressViewStyle(.circular)
                } else {
                    HStack {
                        MoreActionButton(color: .more.importantBright) {
                            contentViewModel.showLoginView()
                        } label: {
                            Text("Decline")
                        }
                        Spacer()
                        MoreActionButton {
                            viewModel.acceptConsent()
                        } label: {
                            Text("Accept")
                        } 
                    }
                }
            }
        } topBarContent: {
            EmptyView()
        }

    }
}

struct ConsentView_Previews: PreviewProvider {
    static var previews: some View {
        ConsentView(viewModel: ConsentViewModel(registrationService: RegistrationService(sharedStorageRepository: UserDefaultsRepository())))
            .environmentObject(ContentViewModel())
    }
}
