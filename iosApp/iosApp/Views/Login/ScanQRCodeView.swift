//
//  Untitled.swift
//  iosApp
//
//  Created by Isabella Aigner on 04.06.25.
//  Copyright © 2025 Redlink GmbH. All rights reserved.
//

import shared

import SwiftUI
import AVFoundation

struct ScanQRCodeView: View {
    @StateObject private var viewModel = ScanQRCodeViewModel()
    @ObservedObject var model: LoginViewModel
     
    private let stringTable = "LoginView"
    
    // Error Properties
    @State private var errorMessage: String = ""
    @Environment(\.openURL) private var openURL
    
    var body: some View {
        VStack(spacing: 8) {
            Button {
                model.showQRCodeView = false
            } label: {
                Image(systemName: "xmark")
                    .foregroundColor(.more.textDefault)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            
            Image("more_welcome")
                .padding(.top, 20)
                .padding(.bottom, 10)
            
            HStack(spacing: 8) {
                Image(systemName: "qrcode.viewfinder")
                    .font(.largeTitle)
                    .foregroundColor(.more.textDefault)
                
                Text(verbatim:.localize(forKey: "scan_qr_code", withComment: "Login with QR Code.", inTable: stringTable))
                    .foregroundColor(.more.primary)
            }
            
            Spacer(minLength: 0)
            
            /// Scanner Frame
            GeometryReader {
                let size = $0.size
                
                QRCodeCameraView(frameSize: CGSize(width: size.width, height: size.height), cameraSession: $viewModel.cameraSession)
                    .onAppear() {
                        viewModel.setupCamera()
                    }
                
                ZStack {
                    ForEach(0...4, id: \.self) { index in
                        let rotation = Double(index) * 90
                        RoundedRectangle(cornerRadius: 2, style: .circular)
                            .trim(from: 0.61, to: 0.64)
                            .stroke(Color("Secondary"), style: StrokeStyle(lineWidth: 3, lineCap: .round, lineJoin: .round))
                            .rotationEffect(.init(degrees: rotation))
                    }
                }
                .frame(width: size.width, height: size.width)
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            }
            
            Spacer(minLength: 45)
        }
        .padding(15)
        // check camera permission
        .onAppear(perform: viewModel.checkCameraPermission)
        .alert(isPresented: $viewModel.showError) {
            Alert(
                title: Text(verbatim:.localize(forKey: "permission_needed", withComment: "Camera permission is needed, but not granted.", inTable: stringTable)),
                message: Text(viewModel.errorMessage),
                primaryButton: .default(Text(verbatim:.localize(forKey: "open_settings", withComment: "Open device settings to grant permissions.", inTable: stringTable)), action: {
                    let settingsString = UIApplication.openSettingsURLString
                    if let settingsURL = URL(string: settingsString) {
                        // open app settings, using openURL SwiftUI API
                        openURL(settingsURL)
                    }
                }),
                secondaryButton: .cancel(Text(verbatim:.localize(forKey: "cancel", withComment: "Cancel QR Code scan", inTable: stringTable)))
            )
        }
        .onChange(of: viewModel.scannedCode) { code in
            if let code {
                model.extractValuesFromQRCode(qrCodeUrl: code)
                model.showQRCodeView = false // View schließen
            }
        }
    }
}

struct ScanQRCodeView_Previews: PreviewProvider {
    static var previews: some View {
        ScanQRCodeView(model: LoginViewModel(registrationService: RegistrationService(shared: Shared(localNotificationListener: LocalPushNotifications(), sharedStorageRepository: UserDefaultsRepository(), observationDataManager: iOSObservationDataManager(), mainBluetoothConnector: IOSBluetoothConnector(), observationFactory: IOSObservationFactory(dataManager: iOSObservationDataManager()), dataRecorder: IOSDataRecorder()))))
    }
}

