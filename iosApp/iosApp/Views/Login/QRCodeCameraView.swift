//
//  Untitled.swift
//  iosApp
//
//  Created by Isabella Aigner on 04.06.25.
//  Copyright © 2025 Redlink GmbH. All rights reserved.
//  Copyright © 2024 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung
//  Licensed under the Apache 2.0 license
//  (see https://www.apache.org/licenses/LICENSE-2.0).
//

import AVKit
import SwiftUI

// Camera View using built in AVCaptureVideoPreviewLayer
struct QRCodeCameraView: UIViewRepresentable {
    var frameSize: CGSize
    @Binding var cameraSession: AVCaptureSession

    func makeUIView(context: Context) -> UIView {
        let view = CameraPreviewView()
        view.configure(session: cameraSession)

        return view
    }

    func updateUIView(_ uiView: UIViewType, context: Context) {
        uiView.setNeedsLayout()
    }
}

struct QRCodeCameraView_Previews: PreviewProvider {
    @State static var previewSession = AVCaptureSession()

    static var previews: some View {
        QRCodeCameraView(
            frameSize: CGSize(width: 300, height: 300),
            cameraSession: $previewSession
        )
    }
}
