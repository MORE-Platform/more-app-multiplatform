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
//  Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
//

import Combine
import KMPNativeCoroutinesCombine
import SwiftUI
import shared

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
    let horizontalContentPadding: CGFloat = 24

    @Published var navigationStack: [NavigationScreen] = []
    @Published var navigationStateStack: [NavigationState] = []

    @Published var fullscreenNavigationStack: [NavigationScreen] = []
    @Published var fullscreenNavigationStateStack: [NavigationState] = []

    @Published var navigationActions: [NavigationActions] = []

    @Published var studyIsUpdating: Bool = false
    @Published var currentStudyState: StudyState = .none

    @Published var studyLoadingError: Bool = false

    @Published var tagState: Int = 0 {
        didSet {
            if let onReset = currentNavigationAction()?.onReset {
                onReset()
            }
        }
    }

    private var cancellables: Set<AnyCancellable> = []

    init(repos: MainRepository) {
        createPublisher(for: repos.study.studyState)
            .removeDuplicates()
            .receive(on: DispatchQueue.main)
            .sink(receiveCompletion: { _ in }) { [weak self] state in
                self?.currentStudyState = state
                if state == StudyState.closed || state == StudyState.paused {
                    self?.clearViews()
                }
            }
            .store(in: &cancellables)

        createPublisher(for: ViewManager.shared.studyLoadingError)
            .removeDuplicates()
            .receive(on: DispatchQueue.main)
            .sink(receiveCompletion: { _ in }) { [weak self] studyLoadingError in
                self?.studyLoadingError = studyLoadingError.boolValue
            }
            .store(in: &cancellables)

        createPublisher(for: ViewManager.shared.showGarminConnectView)
            .removeDuplicates()
            .map {
                $0.boolValue
            }
            .flatMap { show -> AnyPublisher<Bool, Never> in
                if show {
                    return Just(true)
                        .delay(for: .seconds(0.5), scheduler: DispatchQueue.global(qos: .userInitiated))
                        .eraseToAnyPublisher()
                } else {
                    return Just(false).eraseToAnyPublisher()
                }
            }
            .receive(on: DispatchQueue.main)
            .sink(receiveCompletion: { _ in }) { [weak self] show in
                Task {@MainActor in
                    if show {
                        self?.openView(screen: .garminConnect)
                    } else {
                        self?.closeView(screen: .garminConnect)
                    }
                }
            }
            .store(in: &cancellables)
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
                    Task { @MainActor in                    
                        if newValue {
                            self.openView(screen: screen)
                        } else {
                            self.closeView(screen: screen)
                        }
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

    func currentNavigationAction() -> NavigationActions? {
        navigationActions.last
    }

    func currentScreen() -> NavigationScreen? {
        if !navigationStack.isEmpty {
            return navigationStack.last
        }
        return nil
    }

    @MainActor
    func openView(screen: NavigationScreen, scheduleId: String? = nil, observationId: String? = nil, notificationId: String? = nil) {
        if mayChangeViewStructure() {
            if !screen.values.fullScreen {
                switch screen.values.navigationLink {
                case .dashboard:
                    tagState = 0
                case .notifications:
                    tagState = 1
                case .info:
                    tagState = 2
                default:
                    navigationStateStack.append(NavigationState(scheduleId: scheduleId, observationId: observationId, notificationId: notificationId))
                    navigationStack.append(screen)
                    if let onViewOpen = currentNavigationAction()?.onViewOpen {
                        Task { @MainActor in
                            onViewOpen(screen)
                        }
                    }
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
            index > -1
        {
            return navigationStateStack[index]
        } else if screen.values.fullScreen && !fullscreenNavigationStateStack.isEmpty, let index = fullscreenNavigationStack.lastIndex(where: { $0 == screen }),
            index > -1
        {
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
        _ = navigationActions.removeFirst()
    }

    func removeNavigationAction() {
        if !navigationActions.isEmpty {
            _ = navigationActions.popLast()
        }
    }

    func mayChangeViewStructure() -> Bool {
        let notUpdatingOrError = !studyIsUpdating && !studyLoadingError
        let allowedState = currentStudyState == .active || currentStudyState == .none
        return notUpdatingOrError && allowedState
    }

    func openWithDeepLink(url: URL, notificationId: String? = nil) {
        guard !ViewManager.shared.studyIsUpdatingValue else {
            return
        }
        AppDelegate.shared.deeplinkManager.modifyDeepLink(deepLink: url.absoluteString) { modifiedDeepLink in
            if let modifiedDeepLink {
                if let match = NavigationScreen.match(from: modifiedDeepLink.route) {
                    let params = match.params

                    let observationId = params[.observationId]
                    let notificationId = params[.notificationId] ?? notificationId
                    let scheduleId = params[.scheduleId]

                    Task {@MainActor in
                        self.openView(screen: match.screen, scheduleId: scheduleId, observationId: observationId, notificationId: notificationId)
                    }
                }

            } else if modifiedDeepLink == nil, let notificationId {
                AppDelegate.shared.notificationManager.markNotificationAsRead(notificationId: notificationId)
            }
        }
    }

    func openRoute(to data: DeepLinkData) {
        if let url = URL(string: data.route), let match = NavigationScreen.match(from: url) {
            let params = data.params

            let observationId: String? = params[NavigationRouteParameter.observationId.key] as? String
            let notificationId: String? = params[NavigationRouteParameter.notificationId.key] as? String
            let scheduleId: String? = params[NavigationRouteParameter.scheduleId.key] as? String

            Task {@MainActor in
                self.openView(screen: match.screen, scheduleId: scheduleId, observationId: observationId, notificationId: notificationId)
            }
        }
    }
}
