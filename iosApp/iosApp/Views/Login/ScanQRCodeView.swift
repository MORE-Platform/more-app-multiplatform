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
    @StateObject var model: LoginViewModel
    private let stringTable = "LoginView"
    
    // QR Code Scanner Properties
    @State private var isScanning: Bool = false
    @State private var session: AVCaptureSession = .init()
    @State private var cameraPermission: CameraPermissionStatus = .idle
    
    // QR Code Scanner Output
    @State private var qrOutput: AVCaptureMetadataOutput = .init()
    
    // Error Properties
    @State private var errorMessage: String = ""
    @State private var showError: Bool = false
    
    var body: some View {
        VStack(spacing: 8) {
            Button {
                model.showQRCodeView = false
            } label: {
                Image(systemName: "xmark")
                    .foregroundColor(.more.textDefault)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            
            Text(verbatim:.localize(forKey: "scan_qr_code", withComment: "Login with QR Code.", inTable: stringTable))
                .foregroundColor(.more.primaryLight200)
                .padding(.top, 20)
            
            Text(verbatim:.localize(forKey: "scan_start_automatic", withComment: "Scanning will start automatically", inTable: stringTable))
                .foregroundColor(.more.primaryLight200)
                .padding(.top, 20)
            
            Spacer(minLength: 0)
            
            /// Scanner
            GeometryReader {
                let size = $0.size
                
                QRCodeCameraView(frameSize: size, session: $session)
                
                ZStack {
                    ForEach(0...4, id: \.self) { index in
                        let rotation = Double(index) * 90
                        
                        RoundedRectangle(cornerRadius: 2, style: .circular)
                            .trim(from: 0.61, to: 0.64)
                            .stroke(Color("Primary"), style: StrokeStyle(lineWidth: 5, lineCap: .round, lineJoin: .round))
                            .rotationEffect(.init(degrees: rotation))
                    }
                }
                .frame(width: size.width, height: size.width)
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            }
            
            Spacer(minLength: 15)
            
            Button {
                
            } label: {
                Image(systemName: "qrcode.viewfinder")
                    .font(.largeTitle)
                    .foregroundColor(.more.textDefault)
            }
            
            Spacer(minLength: 45)
        }
        .padding(15)
        // check camera permission
        .onAppear(perform: checkCameraPermission)
    }
    
    /// Checking Camera Permissions
    func checkCameraPermission() {
        Task {
            switch AVCaptureDevice.authorizationStatus(for: .video) {
            case .authorized:
                cameraPermission = .approved
            case .notDetermined:
                if await AVCaptureDevice.requestAccess(for: .video) {
                    cameraPermission = .approved
                } else {
                    cameraPermission = .denied
                }
            case .denied, .restricted:
                cameraPermission = .denied
            default: break
            }
        }
    }
}

struct ScanQRCodeView_Previews: PreviewProvider {
    static var previews: some View {
        ScanQRCodeView(model: LoginViewModel(registrationService: RegistrationService(shared: Shared(localNotificationListener: LocalPushNotifications(), sharedStorageRepository: UserDefaultsRepository(), observationDataManager: iOSObservationDataManager(), mainBluetoothConnector: IOSBluetoothConnector(), observationFactory: IOSObservationFactory(dataManager: iOSObservationDataManager()), dataRecorder: IOSDataRecorder()))))
    }
}


