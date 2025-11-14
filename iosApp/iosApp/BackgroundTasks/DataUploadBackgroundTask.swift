import BackgroundTasks

class DataUploadBackgroundTask {
    static let taskID = "io.redlink.more.app.multiplatform.data-upload"

    static func schedule(earliestBeginDate: Date? = nil) {
        let request = BGProcessingTaskRequest(identifier: taskID)
        if let earliestDate = earliestBeginDate {
            request.earliestBeginDate = earliestDate
        } else {
            request.earliestBeginDate = Calendar.current.date(byAdding: .minute, value: 15, to: Date())
        }
        request.requiresNetworkConnectivity = true
        request.requiresExternalPower = false

        do {
            try BGTaskScheduler.shared.submit(request)
            print("DataUploadBackgroundTask::schedule - Background Task scheduled")
        } catch {
            print("DataUploadBackgroundTask::schedule - Error requesting a background task: \(error.localizedDescription)")
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
        print("DataUploadBackgroundTask::close - Cleaning up resources")
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
        print("DataUploadBackgroundTask::handleRefreshTask - Handling Refresh Task")
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
