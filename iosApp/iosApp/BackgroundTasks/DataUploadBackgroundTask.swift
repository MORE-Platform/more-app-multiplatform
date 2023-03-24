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
    private static let MAX_RETRIES: UInt8 = 5
    static func schedule() {
        let request = BGProcessingTaskRequest(identifier: taskID)
        request.earliestBeginDate = Date(timeIntervalSinceNow: 0)
//        request.earliestBeginDate = Calendar.current.date(byAdding: .minute, value: 15, to: Date())
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
    private var tries: UInt8 = 0

    private func uploadCollectedData(maxRetries: UInt8 = MAX_RETRIES, completion: @escaping (Bool) -> Void) {
        Task(priority: .high) {
            print("Uploading Data in background")
            await self.uploadDataManager.uploadData { success in
                if self.tries >= maxRetries || success {
                    self.uploadDataManager.close()
                    self.tries = 0
                    if success {
                        print("Upload success!")
                    } else if self.tries >= maxRetries {
                        print("Max retries overstepped! Retrying later!")
                    }
                    completion(success)
                } else {
                    print("Upload failure! Retrying...")
                    self.tries += 1
                    Timer.scheduledTimer(withTimeInterval: 10, repeats: false) { _ in
                        self.uploadCollectedData(completion: completion)
                    }
                }
            }
        }
    }
}

extension DataUploadBackgroundTask: BackgroundTaskHandler {
    func handleProcessingTask(task: BGProcessingTask) {
        uploadCollectedData {
            DataUploadBackgroundTask.schedule()
            task.setTaskCompleted(success: $0)
        }
    }

    func handleRefreshTask(task: BGAppRefreshTask) {
        uploadCollectedData(maxRetries: 1) {
            task.setTaskCompleted(success: $0)
        }
    }
}
