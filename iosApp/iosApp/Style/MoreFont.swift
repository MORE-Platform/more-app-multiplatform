//
//  FontExtension.swift
//  iosApp
//
//  Created by Jan Cortiel on 06.02.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import Foundation
import SwiftUI

extension Font {
    static let more = Font.More()
    
    struct More {
        let title = Font.title
        let headline = Font.headline
    }
}
