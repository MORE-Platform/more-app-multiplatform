//
//  DataUploadBackgroundTask.swift
//  iosApp
//
//  Created by Jan Cortiel on 23.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import BackgroundTasks
import Foundation

class DataUploadBackgroundTask {
    static let taskID = "io.redlink.more.app.multiplatform.data-upload"
    static func schedule() {
        let request = BGProcessingTaskRequest(identifier: taskID)
        request.earliestBeginDate = Calendar.current.date(byAdding: .minute, value: 15, to: Date())
        request.requiresNetworkConnectivity = true
        request.requiresExternalPower = false

        do {
            try BGTaskScheduler.shared.submit(request)
            print("Background Task scheduled")
        } catch {
            print("Error requesting for a background task")
        }
    }

    private let uploadDataManager = DataUploadManager()

    private func uploadCollectedData(completion: @escaping (Bool) -> Void) {
        Task {
            print("Uploading Data in background")
            await self.uploadDataManager.uploadData { success in
                if success {
                    print("Upload success!")
                } else {
                    print("Could not upload!")
                }
                self.uploadDataManager.close()
                completion(success)
            }
        }
    }
}

extension DataUploadBackgroundTask: BackgroundTaskHandler {
    func handleProcessingTask(task: BGProcessingTask) {
        print("Starting Background Processing Task")
        task.expirationHandler = {
            print("Task will soon expire! Cleaning up...")
            self.uploadDataManager.close()
            DataUploadBackgroundTask.schedule()
            DispatchQueue.main.async {
                print("Cleaned up!")
                task.setTaskCompleted(success: false)
            }
        }
        uploadCollectedData { success in
            DataUploadBackgroundTask.schedule()
            DispatchQueue.main.async {
                print("Task finished with success: \(success)")
                task.setTaskCompleted(success: success)
            }
        }
    }

    func handleRefreshTask(task: BGAppRefreshTask) {
        print("Starting Background Refresh Task")
        task.expirationHandler = {
            print("Task will soon expire! Cleaning up...")
            self.uploadDataManager.close()
            DataUploadBackgroundTask.schedule()
            DispatchQueue.main.async {
                print("Cleaned up!")
                task.setTaskCompleted(success: false)
            }
        }
        uploadCollectedData { success in
            DispatchQueue.main.async {
                print("Task finished with success: \(success)")
                task.setTaskCompleted(success: success)
            }
        }
    }
    
}
