//
//  StringExtension.swift
//  iosApp
//
//  Created by Jan Cortiel on 06.02.23.
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
import CryptoKit

extension String {
    func localize(withComment comment: String, useTable table: String? = nil) -> String {
        var result = NSLocalizedString(self, tableName: table, comment: comment)
        if result == self {
            result = NSLocalizedString(self, tableName: table, comment: comment)
        }
        return result
    }
    static func localize(forKey key: String, withComment comment: String, inTable table: String? = nil) -> String {
        return key.localize(withComment: comment, useTable: table)
    }
    
    func toMD5() -> String {
        let digest = Insecure.MD5.hash(data: self.data(using: .utf8) ?? Data())
        return Data(digest).base64EncodedString()
    }
}
