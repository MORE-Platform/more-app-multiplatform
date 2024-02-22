//
//  NavigationScreens.swift
//  iosApp
//
//  Created by Jan Cortiel on 15.03.23.
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

struct NavigationScreenValues {
    let screenName: String
    let navigationLink: String
    var parameters: [NavigationParameter] = []
}

enum NavigationParameter: String {
    case observationId
    case notificaitonId
    case scheduleId
}

enum NavigationScreens: CaseIterable {
    case dashboard
    case notifications
    case info
    case settings
    case taskDetails
    case studyDetails
    case scanQRCode
    case questionObservation
    case questionObservationThanks
    case dashboardFilter
    case notificationFilter
    case pastObservations
    case runningObservations
    case observationDetails
    case withdrawStudy
    case withdrawStudyConfirm
    case limeSurvey

    var values: NavigationScreenValues {
        switch self {
        case .dashboard:
            return NavigationScreenValues(screenName: "Dashboard", navigationLink: "/dashboard")
        case .notifications:
            return NavigationScreenValues(screenName: "Notifications", navigationLink: "/notifications")
        case .info:
            return NavigationScreenValues(screenName: "Information", navigationLink: "/info")
        case .settings:
            return NavigationScreenValues(screenName: "Settings", navigationLink: "/settings")
        case .taskDetails:
            return NavigationScreenValues(screenName: "Task Details", navigationLink: "/task-details", parameters: [.observationId, .notificaitonId, .scheduleId])
        case .studyDetails:
            return NavigationScreenValues(screenName: "Study Details", navigationLink: "/study-details")
        case .scanQRCode:
            return NavigationScreenValues(screenName: "Scan QR Code", navigationLink: "/scan-qr-code")
        case .questionObservation:
            return NavigationScreenValues(screenName: "Question Observation", navigationLink: "/question-observation", parameters: [.observationId, .notificaitonId, .scheduleId])
        case .questionObservationThanks:
            return NavigationScreenValues(screenName: "Question Thanks", navigationLink: "/question-thanks")
        case .dashboardFilter:
            return NavigationScreenValues(screenName: "Dashboard Filter", navigationLink: "/dashboard-filter")
        case .notificationFilter:
            return NavigationScreenValues(screenName: "Notification Filter", navigationLink: "/notification-filter")
        case .pastObservations:
            return NavigationScreenValues(screenName: "Past Observations", navigationLink: "/past-observations")
        case .runningObservations:
            return NavigationScreenValues(screenName: "Running Observations", navigationLink: "/running-observations")
        case .observationDetails:
            return NavigationScreenValues(screenName: "Observation Details", navigationLink: "/observation-details", parameters: [.observationId])
        case .withdrawStudy:
            return NavigationScreenValues(screenName: "Leave Study", navigationLink: "/leave-study")
        case .withdrawStudyConfirm:
            return NavigationScreenValues(screenName: "Confirm to leave the study", navigationLink: "/confirm-leave-study")
        case .limeSurvey:
            return NavigationScreenValues(screenName: "LimeSurvey", navigationLink: "/lime-survey-observation", parameters: [.observationId, .notificaitonId, .scheduleId])
        }
    }

    static let PARAM_OBSERVATION_ID = "observationId"
    static let PARAM_NOTIFICATION_ID = "notificationId"
}

extension NavigationScreens {
    func localize(useTable table: String, withComment comment: String) -> String {
        return values.screenName.localize(withComment: comment, useTable: table)
    }

    func generateURL(withParameters params: [NavigationParameter: String]) -> URL? {
        var components = URLComponents()
        components.path = values.navigationLink

        var queryItems: [URLQueryItem] = []
        for parameter in values.parameters {
            if let value = params[parameter] {
                queryItems.append(URLQueryItem(name: parameter.rawValue, value: value))
            }
        }

        components.queryItems = queryItems

        return components.url
    }
}
