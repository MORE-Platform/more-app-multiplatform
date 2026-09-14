//
//  Copyright © 2024 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung
//  Licensed under the Apache 2.0 license
//  (see https://www.apache.org/licenses/LICENSE-2.0).
//

import Foundation
import shared

extension Set where Element == String {
    func anyNameIn(items: Set<BluetoothDeviceEntity>) -> Bool {
        contains { name in
            items.contains { item in
                item.deviceName?.contains(name) ?? false
            }
        }
    }
}

extension Set where Element == BluetoothDeviceEntity {
    func deviceWithNameIn(nameSet: Set<String>) -> [BluetoothDeviceEntity] {
        filter { device in
            nameSet.contains { device.deviceName?.contains($0) ?? false }
        }
    }
}
