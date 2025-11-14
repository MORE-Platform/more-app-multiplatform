//
//  MoreAnimationSpeed.swift
//  iosApp
//
//  Created by Jan Cortiel on 06.02.23.
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

extension Animation {
    
    static let more = MoreAnimation()
    
    struct MoreAnimation {
        let foldingAnimation = Animation.easeInOut(duration: .more.medium)
    }
}

extension Double {
    
    static let more = MoreAnimationSpeed()
    
    struct MoreAnimationSpeed {
        let slow = 0.5
        let medium = 0.3
        let fast = 0.1
    }
}
