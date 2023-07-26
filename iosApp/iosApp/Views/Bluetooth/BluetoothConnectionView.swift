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
    @Binding var viewOpen: Bool
    var showAsSeparateView: Bool = false
    
    private let bluetoothStrings = "BluetoothConnection"
    var body: some View {
        MoreMainBackgroundView {
            VStack {
                if showAsSeparateView {
                    HStack(alignment: .lastTextBaseline) {
                        Spacer()
                        VStack(alignment: .center) {
                            Button {
                                viewOpen = false
                            } label: {
                                Text("Close")
                                    .foregroundColor(.more.primary)
                            }
                            .padding(.vertical)
                        }
                    }
                    .padding(.horizontal, 8)
                }
                ScrollView {
                    LazyVStack(alignment: .leading) {
                        Title(titleText: "External Device Setup".localize(withComment: "External Device Setup Screen", useTable: bluetoothStrings))
                        BasicText(text: "\("Some tasks in this study need certain bluetooth devices to be completed and only activate, once a certain device is connected. Please make sure to turn on and connect these devices".localize(withComment: "Bluetooth necessity description", useTable: bluetoothStrings)):", color: Color.more.secondary)
                            .padding(.vertical, 8)
                        
                        ForEach(viewModel.neededDevices, id: \.self) { device in
                            SectionHeading(sectionTitle: "- \(device)")
                        }
                        
                        if showAsSeparateView {
                            BasicText(text: "You can connect to and disconnect from devices at any time: Info > Devices".localize(withComment: "Connection tutorial", useTable: bluetoothStrings), color: Color.more.secondary)
                                .padding(.top, 8)
                        }
                        
                        Divider()
                            .padding(.bottom, 8)
                        
                        Section(header: SectionHeading(sectionTitle: "Connected devices".localize(withComment: "Connected device section", useTable: bluetoothStrings))) {
                            if viewModel.connectedDevices.isEmpty {
                                EmptyListView(text: "\(String.localize(forKey: "No devices connected", withComment: "No devices connected", inTable: bluetoothStrings))!")
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
                        Divider()
                            .padding(.bottom, 8)
                        Section(header: SectionHeading(sectionTitle: "Discovered devices")) {
                            if viewModel.discoveredDevices.isEmpty {
                                EmptyListView(text: "\(String.localize(forKey: "No devices found nearby", withComment: "No devices found nearby", inTable: bluetoothStrings))!")
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
                                    BasicText(text: "\(String.localize(forKey: "Searching for devices", withComment: "Searching for new devices", inTable: bluetoothStrings))...")
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
}

struct BluetoothConnectionView_Previews: PreviewProvider {
    static var previews: some View {
        BluetoothConnectionView(viewModel: BluetoothConnectionViewModel(), viewOpen: .constant(false))
    }
}
