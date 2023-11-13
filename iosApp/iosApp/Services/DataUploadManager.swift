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
    private let observationDataRepository = ObservationDataRepository()
    private let semaphore = Semaphore()
    private var currentlyUploading = false
    
    func uploadData(completion: @escaping (Bool) -> Void) {
        if !currentlyUploading {
            currentlyUploading = true
            DispatchQueue.global(qos: .background).async {
                print("Fetching Data Bulk...")
                self.observationDataRepository.allAsBulk { [weak self] dataBulk in
                    if let dataBulk, !dataBulk.dataPoints.isEmpty {
                        print("Sending data to backend...")
                        AppDelegate.shared.networkService.iosSendData(data: dataBulk) { weakReference in
                            if let pair = weakReference.value {
                                if let error = pair.second {
                                    print("Error: \(error)")
                                    self?.currentlyUploading = false
                                    completion(false)
                                } else if let self, let idSet = pair.first as? Set<String> {
                                    print("Sent data! Deleting local data...")
                                    self.observationDataRepository.deleteAllWithId(idSet: idSet)
                                    print("Deleted data!")
                                    self.currentlyUploading = false
                                    completion(true)
                                } else {
                                    print("Error!")
                                    self?.currentlyUploading = false
                                    completion(false)
                                }
                            }
                        }
                    } else {
                        print("No data to send!")
                        self?.currentlyUploading = false
                        completion(true)
                    }
                }
            }
        }
    }
    func close() {
        print("Closed!")
    }
}
