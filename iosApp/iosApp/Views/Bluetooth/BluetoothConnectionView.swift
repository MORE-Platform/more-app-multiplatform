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
        MoreMainBackground {
            ScrollView {
                LazyVStack(alignment: .leading) {
                    Section(header: SectionHeading(sectionTitle: .constant("Connected devices"))) {
                        if viewModel.connectedDevices.isEmpty {
                            EmptyListView(text: "No devices connected!")
                        } else {
                            ForEach(viewModel.connectedDevices, id: \.self.deviceId) { device in
                                if let deviceName = device.deviceName {
                                    VStack(alignment: .leading) {
                                        DetailsTitle(text: deviceName)
                                        Divider()
                                    }
                                    .padding(.vertical, 8)
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
                                if let deviceName = device.deviceName {
                                    VStack(alignment: .leading) {
                                        HStack(alignment: .firstTextBaseline) {
                                            DetailsTitle(text: deviceName)
                                            if let address = device.address, viewModel.connectingDevices.contains(address) {
                                                Spacer()
                                                ProgressView()
                                            }
                                        }
                                        Divider()
                                    }
                                    .padding(.vertical, 8)
                                    .onTapGesture {
                                        viewModel.connectToDevice(device: device)
                                    }
                                } else {
                                    EmptyView()
                                }
                            }
                        }
                        if viewModel.bluetoothIsScanning {
                            HStack {
                                ProgressView()
                                BasicText(text: .constant("Searching for devices..."))
                            }
                        }
                    }
                    .onAppear {
                        viewModel.viewDidAppear()
                    }
                    .onDisappear {
                        viewModel.viewDidDisappear()
                    }
                }
                .padding(.horizontal, 8)
            }
        } topBarContent: {
            EmptyView()
        }
    }
}

struct BluetoothConnectionView_Previews: PreviewProvider {
    static var previews: some View {
        BluetoothConnectionView(viewModel: BluetoothConnectionViewModel(bluetoothConnector: IOSBluetoothConnector()))
    }
}
