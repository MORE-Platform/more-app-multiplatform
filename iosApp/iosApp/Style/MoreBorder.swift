//
//  MoreBorder.swift
//  iosApp
//
//  Created by Jan Cortiel on 06.02.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import Foundation

extension CGFloat {
    static let moreBorder = MoreBorder()
    
    struct MoreBorder {
        let cornerRadius: CGFloat = 6
        let lineWidth: CGFloat = 2
    }
}
