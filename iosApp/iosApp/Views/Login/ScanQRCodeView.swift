//
//  Untitled.swift
//  iosApp
//
//  Created by Isabella Aigner on 04.06.25.
//  Copyright © 2025 Redlink GmbH. All rights reserved.
//

import AVFoundation
import SwiftUI
import shared

struct ScanQRCodeView: View {
    @StateObject private var viewModel = ScanQRCodeViewModel()
    @ObservedObject var model: LoginViewModel

    // Error Properties
    @State private var errorMessage: String = ""
    @Environment(\.openURL) private var openURL

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

                Text("scan_qr_code")
                    .foregroundColor(.more.primary)
            }

            Spacer(minLength: 0)

            /// Scanner Frame
            ZStack {
                GeometryReader {
                    let size = $0.size

                    QRCodeCameraView(frameSize: CGSize(width: size.width, height: size.height), cameraSession: $viewModel.cameraSession)
                        .onAppear {
                            viewModel.setupCamera()
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

                if viewModel.showError {
                    HStack(spacing: 8) {
                        Text("provide_camera_access")
                            .foregroundColor(.more.important)
                            .padding(.horizontal, 20)
                            .frame(maxWidth: .infinity, alignment: .center)
                            .multilineTextAlignment(.center)
                    }
                }
            }

            Spacer(minLength: 45)
        }
        .padding(15)
        // check camera permission
        .onAppear(perform: viewModel.checkCameraPermission)
        .alert(isPresented: $viewModel.showError) {
            Alert(
                title: Text("permission_needed"),
                message: Text(viewModel.errorMessage),
                primaryButton: .default(
                    Text("open_settings"),
                    action: {
                        let settingsString = UIApplication.openSettingsURLString
                        if let settingsURL = URL(string: settingsString) {
                            // open app settings, using openURL SwiftUI API
                            openURL(settingsURL)
                        }
                    }),
                secondaryButton: .cancel(Text("Cancel"))
            )
        }
        .onChange(of: viewModel.scannedCode) { code in
            if let code {
                model.extractValuesFromQRCode(qrCodeUrl: code)
                model.showQRCodeView = false  // View schließen
            }
        }
    }
}

struct ScanQRCodeView_Previews: PreviewProvider {
    static let database = DatabaseManagerKt.getRoomDatabase(builder: DatabaseManager_iosKt.getDatabaseBuilder())
    static let repos = MainRepositoryImpl(appDatabase: database)
    static var previews: some View {
        ScanQRCodeView(model: LoginViewModel(registration: RegistrationService(shared: Shared(localNotificationListener: LocalPushNotifications(), repositories: repos, sharedStorageRepository: UserDefaultsRepository(), observationDataManager: iOSObservationDataManager(repository: repos), mainBluetoothConnector: IOSBluetoothConnector(), observationFactory: IOSObservationFactory(repository: repos, dataManager: iOSObservationDataManager(repository: repos)), dataRecorder: IOSDataRecorder(), reminderNotificationSchedulingLimit: nil))))
    }
}
