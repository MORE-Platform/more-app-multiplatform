//
//  SimplenavigationModalStateViewModel.swift
//  More
//
//  Created by Isabella Aigner on 02.05.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import shared
import SwiftUI

struct NavigationState {
    var scheduleId: String? = nil	
    var observationId: String? = nil
    var notificationId: String? = nil
}

class NavigationModalState: ObservableObject {
    @Published var activeScreens: [NavigationScreens] = []
    
    @Published var navigationState: NavigationState = NavigationState()
    
    @Published var studyIsUpdating: Bool = false
    @Published var currentStudyState: StudyState = .none
    
    func screenBinding(for screen: NavigationScreens) -> Binding<Bool> {
        Binding<Bool>(
            get: {
                self.activeScreens.contains(screen)
            },
            set: { newValue in
                if self.mayChangeViewStructure() {
                    if newValue {
                        self.activeScreens.append(screen)
                    } else {
                        self.activeScreens.removeAll { $0 == screen }
                    }
                }
            }
        )
    }
    
    func studyIsUpdating(_ updating: Bool) {
        self.studyIsUpdating = updating
        if updating {
            self.clearViews()
        }
    }
    
    func setStudyState(_ state: StudyState) {
        self.currentStudyState = state
        if state == StudyState.closed || state == StudyState.paused {
            self.clearViews()
        }
    }

    func openView(screen: NavigationScreens, scheduleId: String? = nil, observationId: String? = nil, notificationId: String? = nil) {
        if mayChangeViewStructure() {
            navigationState = NavigationState(scheduleId: scheduleId, observationId: observationId, notificationId: notificationId)
            if !activeScreens.contains(screen) {
                activeScreens.append(screen)
            }
        }
    }

    func closeView(screen: NavigationScreens) {
        if mayChangeViewStructure() {
            activeScreens.remove(screen)
        }
    }
    
    func clearViews() {
        self.activeScreens.removeAll()
        self.navigationState = NavigationState()
    }
    
    func mayChangeViewStructure() -> Bool {
        !self.studyIsUpdating && self.currentStudyState == StudyState.active || self.currentStudyState == StudyState.none
    }
    
    func openWithDeepLink(url: URL, notificationId: String? = nil) {
        let path = url.path
        if let matchingScreen = NavigationScreens.allCases.first(where: { $0.values.navigationLink == path }) {
            
            var parameters: [NavigationParameter: String] = [:]
            let components = URLComponents(url: url, resolvingAgainstBaseURL: false)
            
            for queryItem in components?.queryItems ?? [] {
                if let value = queryItem.value, let parameter = NavigationParameter(rawValue: queryItem.name) {
                    parameters[parameter] = value
                }
            }

            let observationId = parameters[.observationId]
            let notificationId = parameters[.notificaitonId] ?? notificationId
            openView(screen: matchingScreen, observationId: observationId, notificationId: notificationId)
        }
    }

}
