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

class QRScannerDelegate: NSObject, AVCaptureMetadataOutputObjectsDelegate {
    var onCodeScanned: ((String) -> Void)?

    func metadataOutput(_ output: AVCaptureMetadataOutput, didOutput metadataObjects: [AVMetadataObject], from connection: AVCaptureConnection) {
        if let metaObject = metadataObjects.first as? AVMetadataMachineReadableCodeObject,
           let code = metaObject.stringValue {
            onCodeScanned?(code)
        }
    }
}

