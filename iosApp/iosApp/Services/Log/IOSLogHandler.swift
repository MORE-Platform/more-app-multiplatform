//
//  IOSLogHandler.swift
//  More
//
//  Created by Jan Cortiel on 04.08.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import Foundation
import shared
//
//class IOSLogHandler: ElasticLogHandler {
//    var logQueue: Set<Log> = Set()
//    func appendNewLog(log: Log) {
//        logQueue.insert(log)
//
//        if logQueue.count > 10 || log.priority == NapierLogLevel.error || log.priority == NapierLogLevel.assert {
//            let arrayCopy = logQueue.map{$0}
//            logQueue.removeAll()
//            sendLogs(logs: arrayCopy)
//        }
//        
//    }
//    
//    private func sendLogs(logs: [Log]) {
//        Task {
//            do {
//                let success = try await AppDelegate.shared.networkService.sendLogs(logs: logs).boolValue
//                if !success {
//                    logs.forEach{ logQueue.insert($0)}
//                }
//            } catch {
//                print("Log upload error: \(error)")
//            }
//        }
//    }
//}
