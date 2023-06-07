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
    var showTutorialDescr: Bool = false
    
    private let bluetoothStrings = "BluetoothConnection"
    var body: some View {
        MoreMainBackgroundView {
            ScrollView {
                LazyVStack(alignment: .leading) {
                    Title(titleText: .constant("External Device Setup".localize(useTable: bluetoothStrings, withComment: "External Device Setup Screen")))
                    BasicText(text: .constant("Some tasks in this study need certain bluetooth devices to be completed and only activate, once a certain device is connected. Please make sure to turn on and connect these devices".localize(useTable: bluetoothStrings, withComment: "Bluetooth necessity description")))
                    
                    ForEach(viewModel.neededDevices, id: \.self) { device in
                        BasicText(text: .constant("- \(device)"))
                    }
                    
                    if showTutorialDescr {
                        BasicText(text: .constant("You can connect to and disconnect from devices at any time: Info > Devices".localize(useTable: bluetoothStrings, withComment: "Connection tutorial")))
                    }
                    
                    Divider()
                    
                    Section(header: SectionHeading(sectionTitle: .constant("Connected devices".localize(useTable: bluetoothStrings, withComment: "Connected device section")))) {
                        if viewModel.connectedDevices.isEmpty {
                            EmptyListView(text: "\(String.localizedString(forKey: "No devices connected", inTable: bluetoothStrings, withComment: "No devices connected"))!")
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
                            EmptyListView(text: "\(String.localizedString(forKey: "No devices found nearby", inTable: bluetoothStrings, withComment: "No devices found nearby"))!")
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
                                BasicText(text: .constant("\(String.localizedString(forKey: "Searching for devices", inTable: bluetoothStrings, withComment: "Searching for new devices"))..."))
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
        } 
    }
}

struct BluetoothConnectionView_Previews: PreviewProvider {
    static var previews: some View {
        BluetoothConnectionView(viewModel: BluetoothConnectionViewModel())
    }
}
