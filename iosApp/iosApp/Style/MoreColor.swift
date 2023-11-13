//
//  ColorExtension.swift
//  iosApp
//
//  Created by Jan Cortiel on 03.02.23.
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

extension Color {
    static let more = Color.MoreColor()
    
    struct MoreColor {
        let primaryDark = Color("PrimaryDark")
        let primary = Color("Primary")
        let primaryMedium = Color("PrimaryMedium")
        let primaryLight200 = Color("PrimaryLight200")
        let primaryLight = Color("PrimaryLight")
        
        let secondary = Color("Secondary")
        let secondaryMedium = Color("SecondaryMedium")
        let secondaryLight = Color("SecondaryLight")
        
        let textDefault = Color("Secondary")
        let textInactive = Color("SecondaryMedium")
        
        let important = Color("Important")
        let importantMedium = Color("ImportantMedium")
        let importantLight = Color("ImportantLight")
        
        let approved = Color("Approved")
        let approvedMedium = Color("ApprovedMedium")
        let approvedLight = Color("ApprovedLight")
        
        let white = Color("White")
        
        // special elements
        let divider = Color("PrimaryLight")
        let mainBackground = Color("SecondaryLight")
    }
}
