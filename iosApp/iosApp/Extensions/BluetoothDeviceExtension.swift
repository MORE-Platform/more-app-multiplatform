//
//  BluetoothDeviceExtension.swift
//  More
//
//  Created by Jan Cortiel on 24.04.23.
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
import PolarBleSdk

extension BluetoothDevice {
    static func fromPolarDevice(polarInfo: PolarDeviceInfo) -> BluetoothDevice {
        BluetoothDevice.Companion().create(deviceId: polarInfo.deviceId, deviceName: polarInfo.name, address: polarInfo.address.uuidString)
    }
}
