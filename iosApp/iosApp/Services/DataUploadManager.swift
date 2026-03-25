//
//  DataUploadManager.swift
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

import Foundation
import shared

class DataUploadManager {
    private let semaphore = Semaphore()
    private var currentlyUploading = false

    func uploadData(completion: @escaping (Bool) -> Void) {
        if !currentlyUploading {
            currentlyUploading = true

            Task(priority: .background) { [weak self] in
                Napier.d("Fetching Data Bulk...")
                let observationDataRepository = await ObservationDataRepositoryImpl(appDatabase: AppDelegate.database)
                do {
                    if let dataBulk = try await observationDataRepository.allAsBulk(), !dataBulk.dataPoints.isEmpty {
                        Napier.d("Sending data to backend...")
                        let pair = try await AppDelegate.shared.networkService.sendData(data: dataBulk)
                        if let error = pair.second {
                            Napier.e("Error: \(error)")
                            self?.currentlyUploading = false
                            completion(false)
                        } else if let self, let idSet = pair.first as? Set<String> {
                            Napier.d("Sent data! Deleting local data...")
                            try await observationDataRepository.deleteAllWithId(idSet: idSet)
                            Napier.d("Deleted data!")
                            self.currentlyUploading = false
                            completion(true)
                        } else {
                            Napier.e("Error!")
                            self?.currentlyUploading = false
                            completion(false)
                        }
                    } else {
                        Napier.d("No data to send!")
                        self?.currentlyUploading = false
                        completion(true)
                    }

                } catch {
                    Napier.e("Error: \(error)")
                    self?.currentlyUploading = false
                    completion(false)
                }
            }
        }
    }

    func close() {
        Napier.d("Closed!")
    }
}
