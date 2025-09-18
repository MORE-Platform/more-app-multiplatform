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
