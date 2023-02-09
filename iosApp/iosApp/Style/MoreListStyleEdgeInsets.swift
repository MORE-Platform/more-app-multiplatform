//
//  MoreListStyle.swift
//  iosApp
//
//  Created by Jan Cortiel on 08.02.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI

extension EdgeInsets {
    static let moreListStyleEdgeInsets = MoreListStyleEdgeInsets()
    
    struct MoreListStyleEdgeInsets {
        let listItem = EdgeInsets(top: .moreListStylePadding.listItemVertical, leading: 0, bottom: .moreListStylePadding.listItemVertical, trailing: 0)
    }
}

extension CGFloat {
    static let moreListStylePadding = MoreListStylePadding()
    
    struct MoreListStylePadding {
        let listItemVertical: CGFloat = 16
    }
}
