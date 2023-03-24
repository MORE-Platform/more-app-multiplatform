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
    
    init() {
        let userDefaults = UserDefaultsRepository()
        networkService = NetworkService(endpointRepository: EndpointRepository(sharedStorageRepository: userDefaults), credentialRepository: CredentialRepository(sharedStorageRepository: userDefaults))
    }
    
    func uploadData(completion: @escaping (Bool) -> Void) async {
        if await self.semaphore.tryLock() {
            Task { @MainActor in
                do {
                    if let dataBulk = try await self.observationDataRepository.allAsBulk() {
                        let result = try await self.networkService.sendData(data: dataBulk)
                        if let networkError = result.second {
                            print(networkError.message)
                            completion(false)
                        } else if let idSet = result.first as? Set<String> {
                            try await self.observationDataRepository.deleteAllWithId(idSet: idSet)
                            await semaphore.unlock()
                            completion(true)
                        }
                    } else {
                        await self.semaphore.unlock()
                    }
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
    
    func close() {
        observationDataRepository.close()
    }
}
