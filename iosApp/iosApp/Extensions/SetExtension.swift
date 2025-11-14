import Foundation
import shared

extension Set where Element == String {
    func anyNameIn(items: Set<BluetoothDevice>) -> Bool {
        contains { name in
            items.contains { item in
                item.deviceName?.contains(name) ?? false
            }
        }
    }
}

extension Set where Element == BluetoothDevice {
    func deviceWithNameIn(nameSet: Set<String>) -> [BluetoothDevice] {
        filter { device in
            nameSet.contains { device.deviceName?.contains($0) ?? false }
        }
    }
}
