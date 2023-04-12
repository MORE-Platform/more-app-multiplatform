//
//  NetworkServiceExtension.swift
//  iosApp
//
//  Created by Jan Cortiel on 08.03.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import Foundation
import shared

extension NetworkService {
    static func create() -> NetworkService {
        let userDefaults = UserDefaultsRepository()
        let credentialsRespository = CredentialRepository(sharedStorageRepository: userDefaults)
        let endpointRepository = EndpointRepository(sharedStorageRepository: userDefaults)
        return NetworkService(endpointRepository: endpointRepository, credentialRepository: credentialsRespository)
    }
}
