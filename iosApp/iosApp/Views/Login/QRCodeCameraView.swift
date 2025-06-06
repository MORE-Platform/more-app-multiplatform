//
//  Untitled.swift
//  iosApp
//
//  Created by Isabella Aigner on 04.06.25.
//  Copyright © 2025 Redlink GmbH. All rights reserved.
//

import SwiftUI
import AVKit

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

/*
struct QRCodeCameraView_Previews: PreviewProvider {
    static var previews: some View {
        QRCodeCameraView(frameSize: CGSize(width: 300, height: 300), session: AVCaptureSession())
    }
}
*/
