//
//  ObservationActionDelegate.swift
//  More
//
//  Created by Jan Cortiel on 11.05.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import Foundation

protocol ObservationActionDelegate {
    func start(scheduleId: String)
    func pause(scheduleId: String)
    func stop(scheduleId: String)
}
