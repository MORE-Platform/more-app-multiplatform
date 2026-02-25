import BackgroundTasks

class DataUploadBackgroundTask {
    static let taskID = "io.redlink.more.app.multiplatform.data-upload"
    static let defaultInterval: TimeInterval = 15 * 60

    static func schedule(earliestBeginDate: Date? = nil) {
        let request = BGProcessingTaskRequest(identifier: taskID)
        if let earliestDate = earliestBeginDate {
            request.earliestBeginDate = earliestDate
        } else {
            request.earliestBeginDate = Date(timeIntervalSinceNow: defaultInterval)
        }
        request.requiresNetworkConnectivity = true
        request.requiresExternalPower = false

        do {
            try BGTaskScheduler.shared.submit(request)
            Napier.i("DataUploadBackgroundTask::schedule - Background Task scheduled for \(request.earliestBeginDate ?? Date())")
        } catch {
            Napier.e("DataUploadBackgroundTask::schedule - Error requesting a background task: \(error.localizedDescription)")
        }
    }

    private let dataCollector = ObservationDataCollector()

    private func collectRecordedData(completion: @escaping () -> Void) {
        Napier.i("DataUploadBackgroundTask::collectRecordedData - \(Date()): Collecting recorded data...")
        dataCollector.collectData { dataCollected in
            if dataCollected {
                Napier.i("DataUploadBackgroundTask::collectRecordedData - \(Date()): Data collected")
            } else {
                Napier.i("DataUploadBackgroundTask::collectRecordedData - \(Date()): No data collected")
            }
            completion()
        }
    }

    private func close() {
        Napier.i("DataUploadBackgroundTask::close - Cleaning up resources")
    }
}

extension DataUploadBackgroundTask: @preconcurrency BackgroundTaskHandler {
    @MainActor
    func handleProcessingTask(task: BGProcessingTask) {
        Napier.i("Starting Background Processing Task")

        task.expirationHandler = {
            Napier.w("\(Date()): Task will soon expire! Cleaning up...")
            self.close()
            DataUploadBackgroundTask.schedule()
            Napier.i("\(Date()): Cleaned up!")
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
        Napier.i("Handling Refresh Task")
        task.setTaskCompleted(success: true)
    }

    static func setupBackgroundTasks() {
        BGTaskScheduler.shared.register(forTaskWithIdentifier: DataUploadBackgroundTask.taskID, using: nil) { task in
            if let processingTask = task as? BGProcessingTask {
                let backgroundTaskHandler = DataUploadBackgroundTask()
                Task {
                    await backgroundTaskHandler.handleProcessingTask(task: processingTask)
                }
            }
        }
    }
}
