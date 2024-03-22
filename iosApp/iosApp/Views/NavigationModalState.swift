//
//  SimplenavigationModalStateViewModel.swift
//  More
//
//  Created by Isabella Aigner on 02.05.23.
//  Copyright © 2023 Ludwig Boltzmann Institute for
//  Digital Health and Prevention - A research institute
//  of the Ludwig Boltzmann Gesellschaft,
//  Oesterreichische Vereinigung zur Foerderung
//  der wissenschaftlichen Forschung
//  Licensed under the Apache 2.0 license with Commons Clause
//  (see https://www.apache.org/licenses/LICENSE-2.0 and
//  https://commonsclause.com/).
//

import shared
import SwiftUI

struct NavigationState: Hashable {
    var scheduleId: String? = nil
    var observationId: String? = nil
    var notificationId: String? = nil
}

struct NavigationActions {
    var onViewOpen: ((NavigationScreen) -> Void)?
    var onBack: (() -> Void)?
    var onReset: (() -> Void)?
}

class NavigationModalState: ObservableObject {

    @Published var navigationStack: [NavigationScreen] = []
    @Published var navigationStateStack: [NavigationState] = []
    
    @Published var fullscreenNavigationStack: [NavigationScreen] = []
    @Published var fullscreenNavigationStateStack: [NavigationState] = []
    
    @Published var navigationActions: [NavigationActions] = []

    @Published var studyIsUpdating: Bool = false
    @Published var currentStudyState: StudyState = .none

    @Published var tagState: Int = 0 {
        didSet {
            if let onReset = currentNavigationAction()?.onReset {
                onReset()
            }
        }
    }

    func screenBinding(for screen: NavigationScreen) -> Binding<Bool> {
        Binding<Bool>(
            get: {
                if !screen.values.fullScreen {
                    self.navigationStack.contains(screen)
                } else {
                    self.fullscreenNavigationStack.contains(screen)
                }
            },
            set: { newValue in
                if self.mayChangeViewStructure() {
                    if newValue {
                        self.openView(screen: screen)
                    } else {
                        self.closeView(screen: screen)
                    }
                }
            }
        )
    }

    func studyIsUpdating(_ updating: Bool) {
        studyIsUpdating = updating
        if updating {
            clearViews()
        }
    }

    func setStudyState(_ state: StudyState) {
        currentStudyState = state
        if state == StudyState.closed || state == StudyState.paused {
            clearViews()
        }
    }
    
    func currentNavigationAction() -> NavigationActions? {
        navigationActions.last
    }
    
    func currentScreen() -> NavigationScreen? {
        if !navigationStack.isEmpty {
            return navigationStack.last
        }
        return nil
    }

    func openView(screen: NavigationScreen, scheduleId: String? = nil, observationId: String? = nil, notificationId: String? = nil) {
        if mayChangeViewStructure() {
            if !screen.values.fullScreen {
                navigationStateStack.append(NavigationState(scheduleId: scheduleId, observationId: observationId, notificationId: notificationId))
                navigationStack.append(screen)
                if let onViewOpen = currentNavigationAction()?.onViewOpen {
                    onViewOpen(screen)
                }
            } else {
                fullscreenNavigationStateStack.append(NavigationState(scheduleId: scheduleId, observationId: observationId, notificationId: notificationId))
                fullscreenNavigationStack.append(screen)
            }
        }
    }

    func navigationState(for screen: NavigationScreen) -> NavigationState? {
        if !screen.values.fullScreen && !navigationStateStack.isEmpty,
           let index = navigationStack.lastIndex(where: { $0 == screen }),
           index > -1 {
            return navigationStateStack[index]
        } else if screen.values.fullScreen && !fullscreenNavigationStateStack.isEmpty, let index = fullscreenNavigationStack.lastIndex(where: { $0 == screen }),
                  index > -1 {
            return fullscreenNavigationStateStack[index]
        }
        return nil
    }

    func popNavigationStack() {
        if !navigationStack.isEmpty, let last = navigationStack.last {
            removeFromStack(screen: last)
        }
    }

    func removeFromStack(screen: NavigationScreen) {
        if mayChangeViewStructure() {
            if !screen.values.fullScreen {
                let screenIndex = navigationStack.pop(screen)
                if screenIndex > -1 {
                    navigationStateStack.remove(at: screenIndex)
                }
            } else {
                let screenIndex = fullscreenNavigationStack.pop(screen)
                if screenIndex > -1 {
                    fullscreenNavigationStateStack.remove(at: screenIndex)
                }
            }
        }
    }

    func closeView(screen: NavigationScreen) {
        removeFromStack(screen: screen)
        if let onBack = currentNavigationAction()?.onBack {
            onBack()
        }
    }

    func clearViews() {
        navigationStack.removeAll()
        navigationStateStack.removeAll()
        fullscreenNavigationStack.removeAll()
        fullscreenNavigationStateStack.removeAll()
        
        if let onReset = currentNavigationAction()?.onReset {
            onReset()
            navigationActions.removeAll()
        }
    }
    
    func pushNavigationAction(actions: NavigationActions) {
        navigationActions.append(actions)
    }
    
    func popNavigationAction() {
        let _ = navigationActions.removeFirst()
    }
    
    func removeNavigationAction() {
        if !navigationActions.isEmpty {
            let _ = navigationActions.popLast()
        }
    }

    func mayChangeViewStructure() -> Bool {
        !studyIsUpdating && currentStudyState == StudyState.active || currentStudyState == StudyState.none
    }

    func openWithDeepLink(url: URL, notificationId: String? = nil) {
        AppDelegate.shared.deeplinkManager.modifyDeepLink(deepLink: url.absoluteString, protocolReplacement: nil, hostReplacement: nil) { modifiedDeepLink in
            if let modifiedDeepLink,
               let modifiedURL = URL(string: modifiedDeepLink) {
                let path = modifiedURL.path
                if let matchingScreen = NavigationScreen.allCases.first(where: { $0.values.navigationLink == path }) {
                    var parameters: [NavigationParameter: String] = [:]
                    let components = URLComponents(url: modifiedURL, resolvingAgainstBaseURL: false)

                    for queryItem in components?.queryItems ?? [] {
                        if let value = queryItem.value, let parameter = NavigationParameter(rawValue: queryItem.name) {
                            parameters[parameter] = value
                        }
                    }

                    let observationId = parameters[.observationId]
                    let notificationId = parameters[.notificaitonId] ?? notificationId
                    let scheduleId = parameters[.scheduleId]

                    self.openView(screen: matchingScreen, scheduleId: scheduleId, observationId: observationId, notificationId: notificationId)
                }
            }
        }
    }
}
