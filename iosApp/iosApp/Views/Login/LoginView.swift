//
//  LoginView.swift
//  iosApp
//
//  Created by Jan Cortiel on 01.02.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI

struct LoginView: View {
    @State var endpoint = "https://"
    @State var key = ""
    var body: some View {
        MoreMainBackgroundView {
            VStack(alignment: .center) {
                
                TextField("Endpoint", text: $endpoint)
                    .padding()
                TextField("ParticipationKey", text: $key)
                    .padding()
                
                Button {
                    
                } label: {
                    Text("Login")
                }
                .foregroundColor(.white)
            }

                
        }
    }
}

struct LoginView_Previews: PreviewProvider {
    static var previews: some View {
        LoginView()
    }
}
