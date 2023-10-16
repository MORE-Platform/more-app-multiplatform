//
//  ArrayExtension.swift
//  iosApp
//
//  Created by Jan Cortiel on 29.03.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import Foundation

extension Array where Element: Collection {
    func flatten() -> [Element.Element] {
        return reduce([],+)
    }
}

extension Array where Element: Equatable {
    mutating func remove(_ elementToRemove: Element) {
        if let i = self.firstIndex(of: elementToRemove) {
            self.remove(at: i)
        }
    }
}
