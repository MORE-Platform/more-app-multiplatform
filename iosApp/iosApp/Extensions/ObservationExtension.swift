//
//  ObservationExtension.swift
//  iosApp
//
//  Created by Jan Cortiel on 23.03.23.
//  Copyright © 2023 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung 
//  Licensed under the Apache 2.0 license with Commons Clause 
//  (see https://www.apache.org/licenses/LICENSE-2.0 and
//  https://commonsclause.com/).
//

import Foundation
import shared
import UIKit

protocol ObservationCollector {
    func collectData(start: Date, end: Date, completion: @escaping () -> Void)
}

extension Observation_ {
    func showPermissionAlert() {
        AlertController.shared.openAlertDialog(model: AlertDialogModel(title: "Required Permissions Were Not Granted", message: "This study requires one or more sensor permissions to function correctly. You may choose to decline these permissions; however, doing so may result in the application and study not functioning fully or as expected. Would you like to navigate to settings to allow the app access to these necessary permissions?", positiveTitle: "Proceed to Settings", negativeTitle: "Proceed Without Granting Permissions", onPositive: {
            if let url = URL(string: UIApplication.openSettingsURLString), UIApplication.shared.canOpenURL(url) {
                UIApplication.shared.open(url, options: [:], completionHandler: nil)
            }
            AlertController.shared.closeAlertDialog()
        }, onNegative: {
            AlertController.shared.closeAlertDialog()
        }))
    }

    func pauseObservation(_ observationType: ObservationType) {
        AppDelegate.shared.observationManager.pauseObservationType(type: observationType.observationType)
    }
}
