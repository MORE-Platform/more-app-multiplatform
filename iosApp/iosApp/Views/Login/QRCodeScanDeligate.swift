//
//  QRCodeScanDeligate.swift
//  iosApp
//
//  Created by Isabella Aigner on 06.06.25.
//  Copyright © 2025 Redlink GmbH. All rights reserved.
//

import SwiftUI
import AVFoundation
import AVKit

class QRScannerDelegate: NSObject, ObservableObject, AVCaptureMetadataOutputObjectsDelegate {
    @Published var scannedCode: String?
    func metadataOutput(_ output: AVCaptureMetadataOutput, didOutput metadataObjects: [AVMetadataObject], from connection: AVCaptureConnection) {
        if let metaObject = metadataObjects.first {
            guard let readableObject = metaObject as? AVMetadataMachineReadableCodeObject else { return }
            guard let code = readableObject.stringValue else { return }
            print(code)
            scannedCode = code
        }
    }
}
