//
//  BackgroundTaskHandler.swift
//  iosApp
//
//  Created by Jan Cortiel on 23.03.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import Foundation
import BackgroundTasks

protocol BackgroundTaskHandler {
    func handleProcessingTask(task: BGProcessingTask)
    func handleRefreshTask(task: BGAppRefreshTask)
}
