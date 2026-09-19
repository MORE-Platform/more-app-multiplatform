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
//  Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
//

import Foundation
import shared

struct NavigationScreenValues {
    let screenName: String
    let navigationLink: NavigationRoute
    var parameters: [NavigationRouteParameter] = []
    var fullScreen: Bool = false
}

enum NavigationScreen: CaseIterable, Equatable, Identifiable {
    var id: Self { self }

    case dashboard
    case notifications
    case info
    case settings
    case bluetoothConnections
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
    case observationErrors
    case garminConnect

    var values: NavigationScreenValues {
        switch self {
        case .dashboard:
            return NavigationScreenValues(screenName: "Dashboard", navigationLink: .dashboard)
        case .notifications:
            return NavigationScreenValues(screenName: "Notifications", navigationLink: .notifications)
        case .info:
            return NavigationScreenValues(screenName: "Information", navigationLink: .info)
        case .settings:
            return NavigationScreenValues(screenName: "Settings", navigationLink: .settings)
        case .bluetoothConnections:
            return NavigationScreenValues(screenName: "Devices", navigationLink: .bluetoothConnection)
        case .taskDetails:
            return NavigationScreenValues(screenName: "Task Details", navigationLink: .scheduleDetails, parameters: [.observationId, .notificationId, .scheduleId])
        case .studyDetails:
            return NavigationScreenValues(screenName: "Study Details", navigationLink: .studyDetails)
        case .scanQRCode:
            return NavigationScreenValues(screenName: "Scan QR Code", navigationLink: .qrCode)
        case .questionObservation:
            return NavigationScreenValues(screenName: "Question Observation", navigationLink: .question, parameters: [.observationId, .notificationId, .scheduleId], fullScreen: true)
        case .questionObservationThanks:
            return NavigationScreenValues(screenName: "Question Thanks", navigationLink: .questionnaireResponse, fullScreen: true)
        case .dashboardFilter:
            return NavigationScreenValues(screenName: "Dashboard Filter", navigationLink: .observationFilter)
        case .notificationFilter:
            return NavigationScreenValues(screenName: "Notification Filter", navigationLink: .notificationFilter)
        case .pastObservations:
            return NavigationScreenValues(screenName: "Past Observations", navigationLink: .completedSchedules)
        case .runningObservations:
            return NavigationScreenValues(screenName: "Running Observations", navigationLink: .runningSchedules)
        case .observationDetails:
            return NavigationScreenValues(screenName: "Observation Details", navigationLink: .observationDetails, parameters: [.observationId])
        case .withdrawStudy:
            return NavigationScreenValues(screenName: "Leave Study", navigationLink: .leaveStudy, fullScreen: true)
        case .withdrawStudyConfirm:
            return NavigationScreenValues(screenName: "Confirm to leave the study", navigationLink: .leaveStudyConfirm, fullScreen: true)
        case .limeSurvey:
            return NavigationScreenValues(screenName: "LimeSurvey", navigationLink: .limesurvey, parameters: [.observationId, .notificationId, .scheduleId], fullScreen: true)
        case .observationErrors:
            return NavigationScreenValues(screenName: "Observation Errors", navigationLink: .observationErrors)
        case .garminConnect:
            return NavigationScreenValues(screenName: "Garmin Connect", navigationLink: .garminConnect, parameters: [], fullScreen: true)
        }
    }

}

extension NavigationScreen {
    static func match(from url: URL) -> (screen: NavigationScreen, params: [NavigationRouteParameter: String])? {
        let path = url.path
        let normalizedURLPath = path.hasPrefix("/") ? String(path.dropFirst()) : path

        // Find a matching screen by comparing normalized paths
        guard
            let screen = NavigationScreen.allCases.first(where: { screen in
                let link = screen.values.navigationLink
                let route = link.route
                let normalizedLink = route.hasPrefix("/") ? String(route.dropFirst()) : route
                return normalizedURLPath == normalizedLink
            })
        else {
            return nil
        }

        // Parse query parameters and keep only the ones declared by the screen
        var resultParams: [NavigationRouteParameter: String] = [:]
        if let components = URLComponents(url: url, resolvingAgainstBaseURL: false) {
            for item in components.queryItems ?? [] {
                if let value = item.value, let param = NavigationRouteParameter.companion.fromKey(key: item.name), screen.values.parameters.contains(param) {
                    resultParams[param] = value
                }
            }
        }

        return (screen, resultParams)
    }
    static func match(from path: String) -> (screen: NavigationScreen, params: [NavigationRouteParameter: String])? {
        let normalizedURLPath = path.hasPrefix("/") ? String(path.dropFirst()) : path

        // Find a matching screen by comparing normalized paths
        guard
            let screen = NavigationScreen.allCases.first(where: { screen in
                let link = screen.values.navigationLink
                let route = link.route
                let normalizedLink = route.hasPrefix("/") ? String(route.dropFirst()) : route
                return normalizedURLPath == normalizedLink
            })
        else {
            return nil
        }

        return (screen, [:])
    }

    func localize() -> String {
        let localizedKey = String.LocalizationValue(stringLiteral: values.screenName)
        return String(localized: localizedKey)
    }

    func generateURL(withParameters params: [NavigationRouteParameter: String]) -> URL? {
        var components = URLComponents()
        components.path = values.navigationLink.route

        var queryItems: [URLQueryItem] = []
        for parameter in values.parameters {
            if let value = params[parameter] {
                queryItems.append(URLQueryItem(name: parameter.key, value: value))
            }
        }

        components.queryItems = queryItems

        return components.url
    }
}
