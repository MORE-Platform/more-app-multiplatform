//
//  ContactInfo.swift
//  More
//
//  Created by Isabella Aigner on 20.04.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import SwiftUI

struct ContactInfo: View {
    @Binding var studyTitle: String
    @Binding var institute: String
    @Binding var contactPerson: String
    @Binding var info: String
    
    let contactEmail: String?
    let contactTel: String?
    
    var body: some View {
        VStack {
            HStack(spacing: 10) {
                Spacer()
                VStack(
                    alignment: .center,
                    spacing: 4
                ) {
                    Spacer()
                    BasicText(
                        text: .constant(studyTitle),
                        color: .more.primaryDark,
                        font: .system(size: 20, weight: .bold)
                    )
                        .padding(.bottom, 18)
                        .multilineTextAlignment(.center)
                    
                    BasicText(
                        text: .constant(institute),
                        font: .system(size: 16, weight: .bold)
                    )
                        .padding(.bottom, 9)
                        .multilineTextAlignment(.center)
                    
                    BasicText(
                        text: .constant(contactPerson),
                        color: .more.secondary,
                        font: .system(size: 16, weight: .semibold)
                    )
                        .padding(.bottom, 0)
                        .multilineTextAlignment(.center)
                    
                    if contactEmail != nil {
                        BasicText(
                            text: .constant(contactEmail ?? ""),
                            color: .more.secondary,
                            font: .system(size: 16)
                        )
                            .padding(.bottom, 0)
                            .multilineTextAlignment(.center)
                    }
                    
                    if contactTel != nil {
                        BasicText(
                            text: .constant(contactTel ?? ""),
                            color: .more.secondary,
                            font: .system(size: 16)
                        )
                            .padding(.bottom, 0)
                            .multilineTextAlignment(.center)
                    }
                    
                    Divider()
                        .frame(height: 36)
                    
                    BasicText(
                        text: .constant(info),
                        color: .more.secondary,
                        font: .system(size: 14)
                    )
                        .multilineTextAlignment(.center)
                    Spacer()
                }
                Spacer()
            }
        }
       
    }
}
