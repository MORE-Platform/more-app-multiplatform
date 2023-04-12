//
//  MoreAnimationSpeed.swift
//  iosApp
//
//  Created by Jan Cortiel on 06.02.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
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
