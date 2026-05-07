//
//  Napier.swift
//  BlendedCare
//
//  Created by Jan Cortiel on 29.01.26.
//  Copyright © 2026 Redlink GmbH. All rights reserved.
//

import Foundation
import shared

enum Napier {
    static func i(
        _ message: String,
        file: String = #fileID,
        function: String = #function,
        line: Int = #line
    ) {
        let tag = "\(file)#\(function):\(line)"
        KMMLogger.shared.i(tag: tag, message: message)
    }

    static func d(
        _ message: String,
        file: String = #fileID,
        function: String = #function,
        line: Int = #line
    ) {
        let tag = "\(file)#\(function):\(line)"
        KMMLogger.shared.d(tag: tag, message: message)
    }

    static func w(
        _ message: String,
        file: String = #fileID,
        function: String = #function,
        line: Int = #line
    ) {
        let tag = "\(file)#\(function):\(line)"
        KMMLogger.shared.w(tag: tag, message: message)
    }

    static func e(
        _ message: String,
        file: String = #fileID,
        function: String = #function,
        line: Int = #line
    ) {
        let tag = "\(file)#\(function):\(line)"
        KMMLogger.shared.e(tag: tag, message: message)
    }

    static func event(_ event: LogEvent, message: String? = nil) {
        KMMLogger.shared.event(event: event, message: message)
    }
}
