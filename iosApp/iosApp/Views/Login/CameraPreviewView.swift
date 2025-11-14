//
//  CameraPreviewView.swift
//  iosApp
//
//  Created by Isabella Aigner on 06.06.25.
//  Copyright © 2025 Redlink GmbH. All rights reserved.
//

import AVFoundation
import UIKit

class CameraPreviewView: UIView {
    private var previewLayer: AVCaptureVideoPreviewLayer?

    func configure(session: AVCaptureSession) {
        if let layer = previewLayer {
            layer.removeFromSuperlayer()
        }

        let layer = AVCaptureVideoPreviewLayer(session: session)
        layer.videoGravity = .resizeAspectFill
        layer.frame = bounds
        self.layer.insertSublayer(layer, at: 0)
        previewLayer = layer
    }

    override func layoutSubviews() {
        super.layoutSubviews()
        previewLayer?.frame = bounds
    }
}
