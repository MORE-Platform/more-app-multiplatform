//
//  DataUploadBackgroundTask.swift
//  iosApp
//
//  Created by Jan Cortiel on 23.03.23.
//  Copyright © 2023 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung 
//  Licensed under the Apache 2.0 license with Commons Clause 
//  (see https://www.apache.org/licenses/LICENSE-2.0 and
//  https://commonsclause.com/).
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
            print("DataUploadBackgroundTask::schedule - Background Task scheduled")
        } catch {
            print("DataUploadBackgroundTask:schedule - Error requesting for a background task")
        }
    }

    private let dataCollector = ObservationDataCollector()
    
    private func collectRecordedData(completion: @escaping () -> Void) {
        print("DataUploadBackgroundTask::collectRecordedData - \(Date()): Collecting recorded data...")
        dataCollector.collectData { dataCollected in
            if dataCollected {
                print("DataUploadBackgroundTask::collectRecordedData - \(Date()): Data collected")
            } else {
                print("DataUploadBackgroundTask::collectRecordedData - \(Date()): No data collected")
            }
            completion()
        }
    }
    
    private func close() {
    }
}

extension DataUploadBackgroundTask: BackgroundTaskHandler {
    @MainActor
    func handleProcessingTask(task: BGProcessingTask) {
        print("DataUploadBackgroundTask::handleProcessingTask - Starting Background Processing Task")
        task.expirationHandler = {
            print("\(Date()): Task will soon expire! Cleaning up...")
            self.close()
            DataUploadBackgroundTask.schedule()
            print("\(Date()): Cleaned up!")
            DispatchQueue.main.async {
                task.setTaskCompleted(success: false)
            }
        }
        collectRecordedData { [weak self] in
            guard let strongSelf = self else {
                DataUploadBackgroundTask.schedule()
                task.setTaskCompleted(success: false)
                return
            }
            
            strongSelf.close()
            DataUploadBackgroundTask.schedule()
            DispatchQueue.main.async {
                task.setTaskCompleted(success: true)
            }
        }

    }

    func handleRefreshTask(task: BGAppRefreshTask) {
        task.setTaskCompleted(success: true)
    }
    
}
