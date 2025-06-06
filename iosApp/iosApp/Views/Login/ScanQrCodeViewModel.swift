//
//  QRCodeCameraModel.swift
//  iosApp
//
//  Created by Isabella Aigner on 06.06.25.
//  Copyright © 2025 Redlink GmbH. All rights reserved.
//

import shared
import SwiftUI
import AVFoundation


enum CameraPermissionStatus: String {
    case idle = "Not Determined"
    case approved = "Access Granted"
    case denied = "Access Denied"
}

class ScanQRCodeViewModel: NSObject, ObservableObject {
    // QR Code Scanner Properties
    @Published var cameraSession = AVCaptureSession()
    @Published var cameraPermission: CameraPermissionStatus = .idle
    @Published var scannedCode: String? = nil
    
    // Error Properties
    @Published var errorMessage: String = ""
    @Published var showError: Bool = false
  
    // QR Code Scanner Output
    private let qrOutput = AVCaptureMetadataOutput()
    // Camera QR Code Output Delegate
    private let qrDelegate = QRScannerDelegate()
    
    override init() {
        super.init()
        qrDelegate.onCodeScanned = { [weak self] code in
            DispatchQueue.main.async {
                self?.scannedCode = code
                self?.cameraSession.stopRunning()
            }
        }
    }

    func checkCameraPermission() {
        Task {
            switch AVCaptureDevice.authorizationStatus(for: .video) {
            case .authorized:
                await MainActor.run {
                    self.cameraPermission = .approved
                    self.setupCamera()
                }
            case .notDetermined:
                let granted = await AVCaptureDevice.requestAccess(for: .video)
                await MainActor.run {
                    if granted {
                        self.cameraPermission = .approved
                        self.setupCamera()
                    } else {
                        self.presentError("Please Provide Access to your Camera")
                    }
                }
            case .denied, .restricted:
                presentError("Please Provide Access to your Camera")
            default:
                break
            }
        }
    }

    func setupCamera() {
        guard let device = AVCaptureDevice.default(for: .video) else {
            presentError("No camera available.")
            return
        }

        do {
            let input = try AVCaptureDeviceInput(device: device)

            if cameraSession.canAddInput(input) {
                cameraSession.addInput(input)
            }

            if cameraSession.canAddOutput(qrOutput) {
                cameraSession.addOutput(qrOutput)
                qrOutput.metadataObjectTypes = [.qr]
                qrOutput.setMetadataObjectsDelegate(qrDelegate, queue: .main)
            }

            cameraSession.startRunning()
        } catch {
            presentError(error.localizedDescription)
        }
    }

    func presentError(_ message: String) {
        DispatchQueue.main.async {
            self.errorMessage = message
            self.showError = true
        }
    }
}
