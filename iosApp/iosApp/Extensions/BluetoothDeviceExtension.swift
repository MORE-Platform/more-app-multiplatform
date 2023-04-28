//
//  BluetoothDeviceExtension.swift
//  More
//
//  Created by Jan Cortiel on 24.04.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import Foundation
import shared
import PolarBleSdk

extension BluetoothDevice {
    static func fromPolarDevice(polarInfo: PolarDeviceInfo) -> BluetoothDevice {
        BluetoothDevice.Companion().create(deviceId: polarInfo.deviceId, deviceName: polarInfo.name, address: polarInfo.address.uuidString, isConnectable: polarInfo.connectable)
    }
}
