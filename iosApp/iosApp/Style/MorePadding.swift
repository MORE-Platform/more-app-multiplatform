//
//  MorePadding.swift
//  iosApp
//
//  Created by Jan Cortiel on 06.02.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import Foundation
import SwiftUI

extension CGFloat {
    static let morePadding = MorePadding()
    
    struct MorePadding {
        let textField: CGFloat = 12
        let textFieldBottom: CGFloat = 24
    }
}

extension EdgeInsets {
    static let moreEdgeInsets = MorePadding()
    
    struct MorePadding {
        let textFieldBottom = EdgeInsets(top: 0, leading: 0, bottom: .morePadding.textFieldBottom, trailing: 0)
    }
}
