//
//  BluetoothConnectionView.swift
//  More
//
//  Created by Jan Cortiel on 24.04.23.
//  Copyright © 2023 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung
//  Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
//

import shared
import SwiftUI

struct BluetoothConnectionView: View {
    @StateObject private var viewModel = BluetoothConnectionViewModel()
    @Binding var viewOpen: Bool
    var showAsSeparateView: Bool = false

    var body: some View {
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
                    Title(titleText: "External Device Setup")
                    BasicText(text: "\(String(localized: "Some tasks in this study need certain bluetooth devices to be completed and only activate, once a certain device is connected. Please make sure to turn on and connect these devices")):", color: Color.more.secondary)
                        .padding(.vertical, 8)

                    ForEach(viewModel.neededDevices, id: \.self) { device in
                        SectionHeading(sectionTitle: "- \(device)")
                    }
                    .padding(.bottom, 8)

                    if showAsSeparateView {
                        BasicText(text: "You can connect to and disconnect from devices at any time: Info > Devices", color: Color.more.secondary)
                            .padding(.top, 8)
                    }

                    if viewModel.bluetoothPower {
                        Section(header: SectionHeading(sectionTitle: "Connected devices")) {
                            if viewModel.connectedDevices.isEmpty {
                                EmptyListView(text: "\("No devices connected")!")
                            } else {
                                ForEach(viewModel.connectedDevices, id: \.self.address) { device in
                                    if let deviceName = device.deviceName {
                                        VStack(alignment: .leading) {
                                            HStack {
                                                DetailsTitle(text: deviceName)
                                                Spacer()
                                                Button(action: {
                                                    viewModel.disconnectFromDevice(device: device)
                                                }, label: {
                                                    Text("Disconnect")
                                                })
                                                .frame(maxWidth: 90)
                                                .padding(4)
                                                .foregroundColor(.more.white)
                                                .background(Color.more.primary)
                                                .cornerRadius(.moreBorder.cornerRadius)
                                                .overlay(
                                                    RoundedRectangle(cornerRadius: .moreBorder.cornerRadius)
                                                        .stroke(Color.more.primary, lineWidth: 1)
                                                )
                                            }
                                            Divider()
                                        }
                                        .padding(.vertical, 12)
                                    }
                                }
                            }
                        }

                        Section(header: SectionHeading(sectionTitle: "Discovered devices")) {
                            if viewModel.discoveredDevices.isEmpty {
                                EmptyListView(text: "\("No devices found nearby")!")
                            } else {
                                ForEach(viewModel.discoveredDevices, id: \.address) { device in
                                    if let deviceName = device.deviceName {
                                        VStack(alignment: .leading) {
                                            HStack {
                                                DetailsTitle(text: deviceName)
                                                if let address = device.address, viewModel.connectingDevices.contains(address) {
                                                    Spacer()
                                                    ProgressView()
                                                        .tint(.more.primary)
                                                }
                                            }
                                            Divider()
                                        }
                                        .frame(height: 30)
                                        .padding(.vertical, 12)
                                        .onTapGesture {
                                            viewModel.connectToDevice(device: device)
                                        }
                                    }
                                }
                            }
                            if viewModel.bluetoothIsScanning {
                                HStack {
                                    ProgressView()
                                        .tint(.more.primary)
                                        .padding(.trailing, 4)
                                    BasicText(text: "\("Searching for devices")...")
                                }
                            }
                        }
                    } else {
                        BasicText(text: "Bluetooth disabled! Please enable to use!")
                    }
                }
                .onAppear {
                    viewModel.viewDidAppear()
                }
                .onDisappear {
                    viewModel.viewDidDisappear()
                }
            }
        }
    }
}

struct BluetoothConnectionView_Previews: PreviewProvider {
    static var previews: some View {
        MoreMainBackgroundView(contentPadding: 8) {
            BluetoothConnectionView(viewOpen: .constant(false))
        }
    }
}
