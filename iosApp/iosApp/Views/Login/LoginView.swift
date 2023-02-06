//
//  LoginView.swift
//  iosApp
//
//  Created by Jan Cortiel on 01.02.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI

struct LoginView: View {
    @StateObject private var model = LoginViewModel()
    @State var endpoint = "https://"
    @State var key = ""
    @State private var endpointShowTextField = false
    @State private var rotationAngle = 0.0

    private let stringTable = "LoginView"
    var body: some View {
        MoreMainBackgroundView {
            VStack(alignment: .center) {
                Title(titleText:
                    .constant(String
                        .localizedString(forKey: "login_welcome_title", inTable: stringTable, withComment: "welcome string on login view")))

                VStack(alignment: .leading) {
                    HStack {
                        SectionHeadling(sectionTitle: .constant(.localizedString(forKey: "study_endpoint_headling", inTable: stringTable, withComment: "headling for endpoint entryfield")))

                        Spacer()
                        UIToggleFoldViewButton(isOpen: $endpointShowTextField)
                    }
                    .padding(.bottom, 4)
                    if endpointShowTextField {
                        MoreTextField(titleKey: .constant(.localizedString(forKey: "study_endpoint_headling", inTable: stringTable, withComment: "input field for endpoint")), inputText: $endpoint)
                    } else {
                        Text(endpoint)
                            .padding(.bottom, 24)
                    }
                    SectionHeadling(sectionTitle: .constant(.localizedString(forKey: "participation_key_entry", inTable: stringTable, withComment: "headline for participation token entry field")))

                    MoreTextField(titleKey: .constant(.localizedString(forKey: "participation_key_entry", inTable: stringTable, withComment: "headline for participation token entry field")), inputText: $key)
                }
                .padding()

                if !model.isLoading {
                    Button {
                        model.validate(token: key, endpoint: endpoint)
                    } label: {
                        Text("Login")
                    }
                    .frame(minWidth: 100, maxWidth: 150)
                    .padding()
                    .foregroundColor(.more.white)
                    .background(Color.more.main)
                    .cornerRadius(6)
                } else {
                    ProgressView()
                        .progressViewStyle(.circular)
                }

                Spacer()
            }
            .frame(maxWidth: 300)
            .padding(.vertical, 50)

        } topBarContent: {
            EmptyView()
        }
    }
}

struct LoginView_Previews: PreviewProvider {
    static var previews: some View {
        LoginView()
    }
}
