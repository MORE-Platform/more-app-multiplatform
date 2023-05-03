//
//  DataUploadManager.swift
//  iosApp
//
//  Created by Jan Cortiel on 23.03.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
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
                        let userDefaults = UserDefaultsRepository()
                        let networkService = NetworkService(endpointRepository: EndpointRepository(sharedStorageRepository: userDefaults), credentialRepository: CredentialRepository(sharedStorageRepository: userDefaults))
                        print("Sending data to backend...")
                        
                        networkService.sendData(data: dataBulk) { [weak self] pair in
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
                            networkService.close()
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
    deinit {
        
    }
}
