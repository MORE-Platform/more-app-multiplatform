//
//  MoreContainerPadding.swift
//  iosApp
//
//  Created by Jan Cortiel on 07.02.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI

extension EdgeInsets {
    static let moreContainerEdgeInsets = MoreContainerStyle()
    
    struct MoreContainerStyle {
        let vertical = EdgeInsets(top: .moreContainerPadding.verticalPadding, leading: 0, bottom: .moreContainerPadding.verticalPadding, trailing: 0)
        let top = EdgeInsets(top: .moreContainerPadding.verticalPadding, leading: 0, bottom: 0, trailing: 0)
        let bottom = EdgeInsets(top: 0, leading: 0, bottom: .moreContainerPadding.verticalPadding, trailing: 0)
        
        let loginVertical = EdgeInsets(top: .moreContainerPadding.verticalLoginPadding, leading: 0, bottom: .moreContainerPadding.verticalLoginPadding, trailing: 0)
    }
}

extension CGFloat {
    static let moreContainerPadding = MoreContainerPadding()
    
    struct MoreContainerPadding {
        let verticalPadding: CGFloat = 24
        let verticalLoginPadding: CGFloat = 50
    }
}
