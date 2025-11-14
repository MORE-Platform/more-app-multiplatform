//
//  MoreListStyle.swift
//  iosApp
//
//  Created by Jan Cortiel on 08.02.23.
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
