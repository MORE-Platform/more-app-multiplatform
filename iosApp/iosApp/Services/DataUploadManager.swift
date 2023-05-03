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
    
    @MainActor
    func uploadData(completion: @escaping (Bool) -> Void) async {
        if await self.semaphore.tryLock() {
            do {
                print("Fetching Data Bulk...")
                if let dataBulk = try await self.observationDataRepository.allAsBulk() {
                    if Task.isCancelled {
                        completion(false)
                        return
                    }
                    if !dataBulk.dataPoints.isEmpty {
                        print("Sending data to backend...")
                        let result = try await AppDelegate.shared.networkService.sendData(data: dataBulk)
                        if Task.isCancelled {
                            completion(false)
                            return
                        }
                        if let networkError = result.second {
                            print("Error: \(networkError.message)")
                            completion(false)
                        } else if let idSet = result.first as? Set<String> {
                            print("Sent data! Deleting data from device...")
                            self.observationDataRepository.deleteAllWithId(idSet: idSet)
                            print("Deleted data!")
                            completion(true)
                        }
                    } else {
                        print("No data to send!")
                        completion(true)
                    }
                } else {
                    print("No data to send!")
                    completion(true)
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
        print("Closed!")
    }
}
