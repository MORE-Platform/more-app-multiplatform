//
//  KotlinLongExtension.swift
//  iosApp
//
//  Created by Julia Mayrhauser on 06.04.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import Foundation
import shared

extension KotlinLong {

    func toInt64() -> Int64 {

        return Int64(truncating: self)

    }

}



