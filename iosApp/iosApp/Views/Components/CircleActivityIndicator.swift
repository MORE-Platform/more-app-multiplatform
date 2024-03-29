//
//  CircleActivityIndicator.swift
//  iosApp
//
//  Created by Isabella Aigner on 21.03.23.
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

struct CircleActivityIndicator: View {
    @State private var isCircleRotating = true
    @State private var animateStart = false
    @State private var animateEnd = true
    
    var body: some View {
        ZStack {
                    Circle()
                        .stroke(lineWidth: 5)
                        .fill(Color.init(red: 0.96, green: 0.96, blue: 0.96))
                        .frame(width: 64, height: 64)
                    
                    Circle()
                        .trim(from: animateStart ? 1/3 : 1/9, to: animateEnd ? 2/5 : 1)
                        .stroke(lineWidth: 5)
                        .rotationEffect(.degrees(isCircleRotating ? 0 : 360))
                        .frame(width: 64, height: 64)
                        .foregroundColor(.more.approved)
                        .onAppear() {
                            withAnimation(Animation
                                            .linear(duration: 1)
                                            .repeatForever(autoreverses: false)) {
                                self.isCircleRotating.toggle()
                            }
                        }
                }
    }
}

struct CircleActivityIndicator_Previews: PreviewProvider {
    static var previews: some View {
        CircleActivityIndicator()
    }
}
