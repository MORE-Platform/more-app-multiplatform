//
//  DictionaryExtension.swift
//  iosApp
//
//  Created by Jan Cortiel on 08.03.23.
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

func += <K, V>(left: inout [K: V], right: [K: V]) {
    for (k, v) in right {
        left[k] = v
    }
}

extension Dictionary {
    func filterValues<V>(predicate: (V) -> Bool) -> [Key: Set<V>] where Value == Set<V> {
        return mapValues { $0.filter(predicate) }
    }
    
    func mapKeys<NewKey>(_ transform: (Key) throws -> NewKey) rethrows -> [NewKey: Value] where NewKey: Hashable {
        try self.reduce(into: [NewKey: Value]()) { result, element in
            let newKey = try transform(element.key)
            result[newKey] = element.value
        }
    }
    
    func mapValues<NewValue>(_ transform: (Value) throws -> NewValue) rethrows -> [Key: NewValue] {
        try self.reduce(into: [Key: NewValue]()) { result, element in
            let newValue = try transform(element.value)
            result[element.key] = newValue
        }
    }
    
}

extension Dictionary where Value == Set<AnyHashable> {
    func flattenValues() -> Set<Value.Element> {
        var resultSet = Set<Value.Element>()
        for valueSet in self.values {
            resultSet.formUnion(valueSet)
        }
        return resultSet
    }
}

