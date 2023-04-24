//
//  BluetoothConnectionView.swift
//  More
//
//  Created by Jan Cortiel on 24.04.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import SwiftUI

struct BluetoothConnectionView: View {
    @StateObject var viewModel: BluetoothConnectionViewModel
    var body: some View {
        ScrollView {
            LazyVStack {
                Section(header: SectionHeading(sectionTitle: .constant("Connected devices"))) {
                    if viewModel.connectedDevices.isEmpty {
                        EmptyListView(text: "No devices connected!")
                    } else {
                        ForEach(viewModel.connectedDevices, id: \.self.deviceId) { device in
                            if let deviceName = device.deviceName, let deviceId = device.deviceId {
                                DetailsTitle(text: deviceName)
                                BasicText(text: .constant(deviceId))
                            } else {
                                EmptyView()
                            }
                        }
                    }
                }
                Section(header: SectionHeading(sectionTitle: .constant("Discovered devices"))) {
                    if viewModel.discoveredDevices.isEmpty {
                        EmptyListView(text: "No devices found nearby!")
                    } else {
                        ForEach(viewModel.discoveredDevices, id: \.self.deviceId) { device in
                            if let deviceName = device.deviceName, let deviceId = device.deviceId {
                                DetailsTitle(text: deviceName)
                                BasicText(text: .constant(deviceId))
                            } else {
                                EmptyView()
                            }
                        }
                    }
                }
            }
            
        }
    }
}

struct BluetoothConnectionView_Previews: PreviewProvider {
    static var previews: some View {
        BluetoothConnectionView(viewModel: BluetoothConnectionViewModel(bluetoothConnector: IOSBluetoothConnector()))
    }
}
