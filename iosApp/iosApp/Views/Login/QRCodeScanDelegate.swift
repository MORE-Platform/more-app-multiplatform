//
//  QRCodeScanDeligate.swift
//  iosApp
//
//  Created by Isabella Aigner on 06.06.25.
//  Copyright © 2025 Redlink GmbH. All rights reserved.
//  Copyright © 2024 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung
//  Licensed under the Apache 2.0 license
//  (see https://www.apache.org/licenses/LICENSE-2.0).
//

import AVFoundation
import AVKit
import SwiftUI

class QRScannerDelegate: NSObject, AVCaptureMetadataOutputObjectsDelegate {
    var onCodeScanned: ((String) -> Void)?

    func metadataOutput(_ output: AVCaptureMetadataOutput, didOutput metadataObjects: [AVMetadataObject], from connection: AVCaptureConnection) {
        if let metaObject = metadataObjects.first as? AVMetadataMachineReadableCodeObject,
           let code = metaObject.stringValue {
            onCodeScanned?(code)
        }
    }
}
