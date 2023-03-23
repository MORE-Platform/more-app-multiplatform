//
//  Semaphore.swift
//  iosApp
//
//  Created by Jan Cortiel on 20.03.23.
//  Copyright © 2023 orgName. All rights reserved.
//

import Foundation

actor Semaphore {
    private var active = false
    
    func tryLock() -> Bool {
        if !active {
            active = true
            return true
        }
        return false
    }
    
    func unlock() {
        active = false
    }
}
