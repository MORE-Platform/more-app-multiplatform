//
//  QRCodeCameraModel.swift
//  iosApp
//
//  Created by Isabella Aigner on 06.06.25.
//  Copyright © 2025 Redlink GmbH. All rights reserved.
//

import AVFoundation
import shared
import SwiftUI

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
                Task.detached { [weak self] in
                    guard let session = self?.cameraSession else { return }
                    session.stopRunning()
                }
            }
        }
    }

    func checkCameraPermission() {
        Task {
            switch AVCaptureDevice.authorizationStatus(for: .video) {
            case .authorized:
                await MainActor.run {
                    self.cameraPermission = .approved
                    Task.detached { [weak self] in
                        self?.setupCamera()
                    }
                }
            case .notDetermined:
                let granted = await AVCaptureDevice.requestAccess(for: .video)
                await MainActor.run {
                    if granted {
                        self.cameraPermission = .approved
                        Task.detached { [weak self] in
                            self?.setupCamera()
                        }
                        self.showError = false
                    } else {
                        self.showError = true
                    }
                }
            case .denied, .restricted:
                self.showError = true
            default:
                break
            }
        }
    }

    func setupCamera() {
        guard let device = AVCaptureDevice.default(for: .video) else {
            Task { @MainActor in
                self.showError = true
            }
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

            Task.detached { [weak self] in
                guard let session = self?.cameraSession else { return }
                session.startRunning()
            }

        } catch {
            presentError(errorDescription: error.localizedDescription)
        }
    }

    func presentError(errorDescription: String?) {
        if errorDescription != nil {
            print("Error when setting up camera: ")
            print(errorDescription! as String)
        }

        DispatchQueue.main.async {
            self.showError = true
        }
    }
}
