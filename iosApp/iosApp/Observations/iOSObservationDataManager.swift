//
//  iOSObservationDataManager.swift
//  iosApp
//
//  Created by Jan Cortiel on 20.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import Foundation
import shared

class iOSObservationDataManager: ObservationDataManager {
    private let networkService: NetworkService
    private let observationDataRepository = ObservationDataRepository()
    private let semaphore = Semaphore()
    
    override init() {
        let userDefaults = UserDefaultsRepository()
        networkService = NetworkService(endpointRepository: EndpointRepository(sharedStorageRepository: userDefaults), credentialRepository: CredentialRepository(sharedStorageRepository: userDefaults))
        super.init()
    }
    
    override func sendData() {
        Task(priority: .utility) { @MainActor in
            if await self.semaphore.tryLock() {
                do {
                    let dataBulk: DataBulk? = try await self.observationDataRepository.allAsBulk()
                    
                    if let dataBulk {
                        self.networkService.sendData(data: dataBulk) { kPair, error in
                            if let networkError = kPair?.second {
                                print(networkError.message)
                            } else if let idSet = kPair?.first as? Set<String> {
                                self.observationDataRepository.deleteAllWithId(idSet: idSet)
                            }
                        }
                    }
                    await self.semaphore.unlock()
                } catch {
                    print("Could not get databulk")
                }
                await self.semaphore.unlock()
            }
        }
    }
}
