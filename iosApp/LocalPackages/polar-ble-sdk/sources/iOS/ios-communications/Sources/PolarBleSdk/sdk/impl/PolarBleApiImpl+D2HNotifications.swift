import Foundation
import RxSwift

extension PolarBleApiImpl: PolarDeviceToHostNotificationsApi {
    func observeDeviceToHostNotifications(identifier: String) -> RxSwift.Observable<PolarD2HNotificationData> {
        do {
            let session = try serviceClientUtils.sessionFtpClientReady(identifier)
            guard let client = session.fetchGattClient(BlePsFtpClient.PSFTP_SERVICE) as? BlePsFtpClient else {
                return Observable.error(PolarErrors.serviceNotFound)
            }
            return client.observeDeviceToHostNotifications(identifier: identifier)
                
        } catch let error {
            return Observable.error(error)
        }
    }
}

extension BlePsFtpClient {
    func observeDeviceToHostNotifications(identifier: String) -> Observable<PolarD2HNotificationData> {
        let receiveNotifications = self.waitNotification()
            .compactMap { notification -> PolarD2HNotificationData? in
                guard let mappedNotification = PolarDeviceToHostNotification(rawValue: Int(notification.id)) else {
                    BleLogger.trace("Unknown notification type: \(notification.id)")
                    return nil
                }
                let parameters = Data(notification.parameters)
                let parsedParameters = Self.parseD2HNotificationParameters(mappedNotification, data: parameters)
                return PolarD2HNotificationData(
                    notificationType: mappedNotification,
                    parameters: parameters,
                    parsedParameters: parsedParameters
                )
            }
            .do(onNext: { data in
                BleLogger.trace("Received D2H notification for \(identifier): \(data.notificationType)")
            }, onError: { error in
                BleLogger.error("D2H notification error for \(identifier): \(error.localizedDescription)")
            })
        return receiveNotifications
    }
    
    fileprivate static func parseD2HNotificationParameters(_ notification: PolarDeviceToHostNotification,
                                                            data: Data) -> Any? {
        if data.isEmpty {
            return nil
        }
        
        do {
            switch notification {
            case .syncRequired:
                return try Protocol_PbPFtpSyncRequiredParams(serializedData: data, extensions: nil)
            case .filesystemModified:
                return try Protocol_PbPFtpFilesystemModifiedParams(serializedData: data, extensions: nil)
            case .inactivityAlert:
                return try Protocol_PbPFtpInactivityAlert(serializedData: data, extensions: nil)
            case .trainingSessionStatus:
                return try Protocol_PbPFtpTrainingSessionStatus(serializedData: data, extensions: nil)
            case .autosyncStatus:
                return try Protocol_PbPFtpAutoSyncStatusParams(serializedData: data, extensions: nil)
            case .pnsDhNotificationResponse:
                return try Protocol_PbPftpPnsDHNotificationResponse(serializedData: data, extensions: nil)
            case .pnsSettings:
                return try Protocol_PbPftpPnsState(serializedData: data, extensions: nil)
            case .startGpsMeasurement:
                return try Protocol_PbPftpStartGPSMeasurement(serializedData: data, extensions: nil)
            case .polarShellDhData:
                return try Protocol_PbPFtpPolarShellMessageParams(serializedData: data, extensions: nil)
            case .mediaControlRequestDh:
                return try Protocol_PbPftpDHMediaControlRequest(serializedData: data, extensions: nil)
            case .mediaControlCommandDh:
                return try Protocol_PbPftpDHMediaControlCommand(serializedData: data, extensions: nil)
            case .mediaControlEnabled:
                return try Protocol_PbPftpDHMediaControlEnabled(serializedData: data, extensions: nil)
            case .restApiEvent:
                return try Protocol_PbPftpDHRestApiEvent(serializedData: data, extensions: nil)
            case .exerciseStatus:
                return try Protocol_PbPftpDHExerciseStatus(serializedData: data, extensions: nil)
            // Notifications without parameters or not yet implemented
            default:
                BleLogger.trace("No parameter parsing implemented for: \(notification)")
                return nil
            }
        } catch {
            BleLogger.error("Failed to parse D2H notification parameters for \(notification): \(error.localizedDescription)")
            return nil
        }
    }
}
