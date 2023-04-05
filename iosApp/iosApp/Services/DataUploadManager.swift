//
//  DataUploadManager.swift
//  iosApp
//
//  Created by Jan Cortiel on 23.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import Foundation
import shared

class DataUploadManager {
    private let networkService: NetworkService
    private let observationDataRepository = ObservationDataRepository()
    private let semaphore = Semaphore()
    private var currentTask: Task<(), Never>? = nil
    
    private var currentJob: Ktor_ioCloseable? = nil
    
    private let lockQueue = DispatchQueue(label: "lock-queue")
    
    init() {
        let userDefaults = UserDefaultsRepository()
        networkService = NetworkService(endpointRepository: EndpointRepository(sharedStorageRepository: userDefaults),
                                        credentialRepository: CredentialRepository(sharedStorageRepository: userDefaults))
    }
    
    @MainActor
    func uploadData(completion: @escaping (Bool) -> Void) async {
        if await self.semaphore.tryLock() {
            do {
                print("Fetching Bulk...")
                if let dataBulk = try await self.observationDataRepository.allAsBulk() {
                    if Task.isCancelled {
                        completion(false)
                        return
                    }
                    if !dataBulk.dataPoints.isEmpty {
                        print("Sending data to backend...")
                        let result = try await self.networkService.sendData(data: dataBulk)
                        if Task.isCancelled {
                            completion(false)
                            return
                        }
                        if let networkError = result.second {
                            print("Error: \(networkError.message)")
                            completion(false)
                        } else if let idSet = result.first as? Set<String> {
                            print("Sent data! Deleting data from device...")
                            try await self.observationDataRepository.deleteAllWithId(idSet: idSet)
                            print("Deleted data!")
                            completion(true)
                        }
                    } else {
                        print("Data Bulk empty!")
                        completion(true)
                    }
                } else {
                    completion(false)
                }
                await self.semaphore.unlock()
            } catch {
                print("Could not get databulk")
                completion(false)
                await self.semaphore.unlock()
            }
        }
        else {
            completion(false)
        }
    }
    
    func close() {
        self.currentJob?.close()
        currentTask?.cancel()
        print("Closed!")
    }
}
