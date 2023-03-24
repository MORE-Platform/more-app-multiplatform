//
//  AppDelegate.swift
//  iosApp
//
//  Created by Jan Cortiel on 23.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import Foundation
import UIKit
import BackgroundTasks

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
        registerBackgroundTasks()
        return true
    }
    
    func registerBackgroundTasks() {
        BGTaskScheduler.shared.register(forTaskWithIdentifier: DataUploadBackgroundTask.taskID, using: nil) { task in
            if let task = task as? BGProcessingTask {
                DataUploadBackgroundTask().handleProcessingTask(task: task)
            }
        }
    }
    
    func scheduleTasks() {
        DataUploadBackgroundTask.schedule()
    }
}
