//
//  ArrayExtension.swift
//  iosApp
//
//  Created by Jan Cortiel on 29.03.23.
//  Copyright © 2023 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung 
//  Licensed under the Apache 2.0 license with Commons Clause 
//  (see https://www.apache.org/licenses/LICENSE-2.0 and
//  https://commonsclause.com/).
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
    
    mutating func pop(_ elementToPop: Element) -> Int {
        if let index = self.lastIndex(of: elementToPop) {
            self.remove(at: index)
            return index
        }
        return -1
    }
}
