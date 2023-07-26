//
//  ContactInfo.swift
//  More
//
//  Created by Isabella Aigner on 20.04.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import SwiftUI

struct ContactInfo: View {
    var title: String
    var info: String
    
    let contactInstitute: String?
    let contactPerson: String?
    let contactEmail: String?
    let contactPhoneNumber: String?
    
    var body: some View {
        VStack {
            HStack(spacing: 10) {
                Spacer()
                VStack(
                    alignment: .center,
                    spacing: 4
                ) {
                    Spacer()
                    if contactPerson != nil || contactEmail != nil || contactPhoneNumber != nil {
                        BasicText(
                            text: title,
                            color: .more.primaryDark,
                            font: .system(size: 20, weight: .bold)
                        )
                            .padding(.bottom, 18)
                            .multilineTextAlignment(.center)
                    }
                    
                    if contactInstitute != nil {
                        BasicText(
                            text: contactInstitute ?? "",
                            font: .system(size: 16, weight: .bold)
                        )
                            .padding(.bottom, 9)
                            .multilineTextAlignment(.center)
                    }
                    
                    if contactPerson != nil {
                        BasicText(
                            text: contactPerson ?? "",
                            color: .more.secondary,
                            font: .system(size: 16, weight: .semibold)
                        )
                            .padding(.bottom, 0)
                            .multilineTextAlignment(.center)
                    }
                    
                    if contactEmail != nil {
                        BasicText(
                            text: contactEmail ?? "",
                            color: .more.secondary,
                            font: .system(size: 16)
                        )
                            .padding(.bottom, 0)
                            .multilineTextAlignment(.center)
                    }
                    
                    if contactPhoneNumber != nil {
                        BasicText(
                            text: contactPhoneNumber ?? "",
                            color: .more.secondary,
                            font: .system(size: 16)
                        )
                            .padding(.bottom, 0)
                            .multilineTextAlignment(.center)
                    }
                    
                    if contactPerson != nil || contactEmail != nil || contactPhoneNumber != nil {
                        Divider()
                            .frame(height: 36)
                        
                        BasicText(
                            text: info,
                            color: .more.secondary,
                            font: .system(size: 14)
                        )
                        .multilineTextAlignment(.center)
                        Spacer()
                    }
                }
                Spacer()
            }
        }
       
    }
}
