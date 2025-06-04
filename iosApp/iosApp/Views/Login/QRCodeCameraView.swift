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
    
    @Binding var session: AVCaptureSession
    
    func makeUIView(context: Context) -> UIView {
        let view = UIViewType(frame: CGRect(origin: .zero, size: frameSize))
        view.backgroundColor = .clear
        
        let cameraLayer = AVCaptureVideoPreviewLayer(session: session)
        cameraLayer.frame = .init(origin: .zero, size: frameSize)
        cameraLayer.videoGravity = .resizeAspectFill
        cameraLayer.masksToBounds = true
        view.layer.addSublayer(cameraLayer)
        
        return view
    }
    
    func updateUIView(_ uiView: UIViewType, context: Context) {
        
    }
}

/*
struct QRCodeCameraView_Previews: PreviewProvider {
    static var previews: some View {
        QRCodeCameraView(frameSize: CGSize(width: 300, height: 300), session: AVCaptureSession())
    }
}
*/
