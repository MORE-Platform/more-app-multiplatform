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

import CryptoKit
import Foundation
import shared

extension String {
    func localize(withComment comment: String, useTable table: String? = nil) -> String {
        let result = NSLocalizedString(self, tableName: table, comment: comment)
        if result.isEmpty {
            return self
        }
        return result
    }

    static func localize(forKey key: String, withComment comment: String, inTable table: String? = nil) -> String {
        return key.localize(withComment: comment, useTable: table)
    }

    func toMD5() -> String {
        let digest = Insecure.MD5.hash(data: data(using: .utf8) ?? Data())
        return Data(digest).base64EncodedString()
    }

    func applyHyperlinks() -> String {
        if #available(iOS 15.0, *) {
            let regex = try! NSRegularExpression(pattern: RegexData.companion.url, options: [])
            let range = NSRange(location: 0, length: utf16.count)
            var markdownString = self
            
            let matches = regex.matches(in: self, options: [], range: range).reversed()
            
            for match in matches {
                guard let range = Range(match.range, in: self) else { continue }
                let url = String(self[range])
                let markdown = "[\(url)](\(url))"
                markdownString = markdownString.replacingCharacters(in: range, with: markdown)
            }
            
            return markdownString
        }
        return self
    }
}
