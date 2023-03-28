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
    
    init() {
        let userDefaults = UserDefaultsRepository()
        networkService = NetworkService(endpointRepository: EndpointRepository(sharedStorageRepository: userDefaults), credentialRepository: CredentialRepository(sharedStorageRepository: userDefaults))
    }
    
    func uploadData(completion: @escaping (Bool) -> Void) async {
        print("Locking Semaphore")

        if await self.semaphore.tryLock() {
            currentTask = Task { @MainActor in
                do {
                    print("Fetching Bulk")
                    if let dataBulk = try await self.observationDataRepository.allAsBulk() {
                        if Task.isCancelled {
                            completion(false)
                            return taskIsCancelled()
                        }
                        if dataBulk.dataPoints.count > 0 {
                            print("Sending data to backend...")
                            let result = try await self.networkService.sendData(data: dataBulk)
                            if Task.isCancelled {
                                completion(false)
                                return taskIsCancelled()
                            }
                            if let networkError = result.second {
                                print(networkError.message)
                                completion(false)
                            } else if let idSet = result.first as? Set<String> {
                                print("Sent data!")
                                try await self.observationDataRepository.deleteAllWithId(idSet: idSet)
                                completion(true)
                            }
                        } else {
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
        }
        else {
            completion(false)
        }
    }
    
    func taskIsCancelled() {
        print("Data Upload task cancelled")
        Task {
            await self.semaphore.unlock()
            currentTask = nil
        }
    }
    
    func close() {
        currentTask?.cancel()
        observationDataRepository.close()
    }
}
