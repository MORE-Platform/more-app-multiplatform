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
    
    init(model: LoginViewModel) {
            _model = StateObject(wrappedValue: model)
        }
    
    private let stringTable = "LoginView"
    
    // QR Code Scanner Properties
    @State private var isScanning: Bool = false
    @State private var cameraSession: AVCaptureSession = .init()
    @State private var cameraPermission: CameraPermissionStatus = .idle
       
    // QR Code Scanner Output
    @State private var qrOutput: AVCaptureMetadataOutput = .init()
    @State private var videoPreviewLayer: AVCaptureVideoPreviewLayer = .init()
    @State private var showError: Bool = false
    
    // Error Properties
    @State private var errorMessage: String = ""
    @Environment(\.openURL) private var openURL
    
    // Camera QR Code Output Delegate
    @StateObject private var qrDelegate = QRScannerDelegate()
    
    // Scanned Code
    @State private var scannedCode: String = ""
    
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
                
                QRCodeCameraView(frameSize: CGSize(width: size.width, height: size.height), cameraSession: $cameraSession)
                    .onAppear() {
                        setupCamera()
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
        .onAppear(perform: checkCameraPermission)
        .alert(isPresented: $showError) {
            Alert(
                title: Text("Permission needed"),
                message: Text(errorMessage),
                primaryButton: .default(Text("Open Settings"), action: {
                    let settingsString = UIApplication.openSettingsURLString
                    if let settingsURL = URL(string: settingsString) {
                        // open app settings, using openURL SwiftUI API
                        openURL(settingsURL)
                    }
                }),
                secondaryButton: .cancel(Text("Cancel"))
            )
        }
        .onChange(of: qrDelegate.scannedCode) { newValue in
            if let code = newValue {
                scannedCode = code
                cameraSession.stopRunning()
                
                // send code to model to put it into Token and url & close view
                
                model.extractValuesFromQRCode(qrCodeUrl: scannedCode)
                print(model.token)
                print(model.defaultEndpoint)
                
                model.showQRCodeView = false
            }
        }
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
                    presentError("Please Provide Access to your Camera for scanning codes")
                }
            case .denied, .restricted:
                cameraPermission = .denied
                presentError("Please Provide Access to your Camera for scanning codes")
            default: break
            }
        }
    }
    
    func presentError(_ message: String) {
        errorMessage = message
        showError.toggle()
    }
    
    
    func setupCamera() {
        do {
            guard let device = AVCaptureDevice.DiscoverySession(deviceTypes: [.builtInWideAngleCamera], mediaType: .video, position: .back).devices.first else {
                presentError("UNKNOWN DEVICE ERROR")
                return
            }
            
            do {
            // Camera input
                let input = try AVCaptureDeviceInput(device: device)
                
                guard cameraSession.canAddInput(input), cameraSession.canAddOutput(qrOutput) else {
                    presentError("UNKNOWN INPUT/OUTPUT ERROR")
                    return
                }
                
                // add input & output to camera session
                cameraSession.beginConfiguration()
                cameraSession.addInput(input)
                cameraSession.addOutput(qrOutput)
                
                // setting outputconfig to reaed qr code
                qrOutput.metadataObjectTypes = [.qr]
                // adding delegate to retrieve the fetched qr code from camera
                qrOutput.setMetadataObjectsDelegate(qrDelegate, queue: .main)
               
                cameraSession.commitConfiguration()
                
                DispatchQueue.global(qos: .background).async {
                    cameraSession.startRunning()
                }
                 
            } catch {
                presentError(error.localizedDescription)
            }
        }
    }
}

/*
struct ScanQRCodeView_Previews: PreviewProvider {
    static var previews: some View {
        ScanQRCodeView(model: LoginViewModel(registrationService: RegistrationService(shared: Shared(localNotificationListener: LocalPushNotifications(), sharedStorageRepository: UserDefaultsRepository(), observationDataManager: iOSObservationDataManager(), mainBluetoothConnector: IOSBluetoothConnector(), observationFactory: IOSObservationFactory(dataManager: iOSObservationDataManager()), dataRecorder: IOSDataRecorder()))))
    }
}
*/

