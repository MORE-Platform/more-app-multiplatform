//
//  ModuleListItem.swift
//  iosApp
//
//  Created by Daniil Barkov on 30.03.23.
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
import shared

struct ModuleListItem: View {
    let observation: ObservationSchema
    
    var body: some View {
        VStack{
            HStack(){
                VStack(alignment: .leading){
                    BasicText(text: observation.observationTitle)
                        .padding(.bottom, (0.5))
                    
                    BasicText(text: observation.observationType, color: Color.more.secondary)
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
