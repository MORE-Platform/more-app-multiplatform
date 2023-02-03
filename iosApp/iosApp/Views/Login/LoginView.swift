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
    @State private var endpointShowTextField = false
    @State private var rotationAngle = 0.0
    @State private var isLoading = false
    var body: some View {
        MoreMainBackgroundView {
            VStack(alignment: .center) {
                
                Text("Welcome to More")
                    .font(.title)
                    .foregroundColor(Color.ui.mainTitle)
                    .fontWeight(.bold)
                
                    
                VStack(alignment: .leading) {
                    HStack {
                        Text("Study Endpoint")
                            .font(.headline)

                        Spacer()
                        Button(action: {
                            withAnimation(.easeInOut(duration: 0.3)) {
                                if !endpointShowTextField {
                                    rotationAngle += 180
                                } else {
                                    rotationAngle -= 180
                                }
                            }
                            endpointShowTextField.toggle()
                        }) {
                            Image(systemName: "chevron.down")
                                .imageScale(.large)
                                .rotationEffect(Angle(degrees: rotationAngle))
                        }
                    }
                    .padding(.bottom, 4)
                    if endpointShowTextField {
                        TextField("Endpoint", text: $endpoint)
                            .padding(12)
                            .overlay(
                                RoundedRectangle(cornerRadius: 6)
                                    .stroke(Color.ui.main, lineWidth: 2)
                            )
                            .padding(.bottom, 24)
                    } else {
                        Text(endpoint)
                            .padding(.bottom, 24)
                    }
                    Text("Registration Token")
                        .font(.headline)
                        
                    TextField("Participation Key", text: $key)
                        .padding(12)
                        .overlay(
                            RoundedRectangle(cornerRadius: 6)
                                .stroke(Color.ui.main, lineWidth: 2)
                        )
                        

                }
                .padding()
                
                if !isLoading {
                    Button {
                        isLoading = true
                    } label: {
                        Text("Login")
                    }
                    .frame(minWidth: 100, maxWidth: 150)
                    .padding()
                    .foregroundColor(.ui.white)
                    .background(Color.ui.main)
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
