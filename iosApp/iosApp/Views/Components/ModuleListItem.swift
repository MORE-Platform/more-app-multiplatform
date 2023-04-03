//
//  ModuleListItem.swift
//  iosApp
//
//  Created by Daniil Barkov on 30.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI
import shared

struct ModuleListItem: View {
    let observation: ObservationSchema
    
    var body: some View {
        VStack{
            HStack(){
                VStack(alignment: .leading){
                    BasicText(text: .constant(observation.observationTitle))
                        .padding(.bottom, (0.5))
                    
                    BasicText(text: .constant(observation.observationType), color: Color.more.secondary)
                }
                Spacer()
                Image(systemName: "chevron.forward")
            }
            Divider()
        }
        
    }
}

struct ModuleListItem_Previews: PreviewProvider {
    static var previews: some View {
        ModuleListItem(observation: ObservationSchema())
    }
}
