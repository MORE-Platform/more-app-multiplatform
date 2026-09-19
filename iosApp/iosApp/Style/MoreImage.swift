//
//  MoreImage.swift
//  iosApp
//
//  Created by Jan Cortiel on 06.02.23.
//  Copyright © 2023 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung
//  Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
//

import SwiftUI

extension Image {
    static let more = MoreImage()

    struct MoreImage {
        let toggleFoldView = Image(systemName: "chevron.down")
    }
}
