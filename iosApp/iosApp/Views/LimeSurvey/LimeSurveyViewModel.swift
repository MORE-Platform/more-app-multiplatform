//
//  LimeSurveyViewModel.swift
//  More
//
//  Created by Jan Cortiel on 10.05.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
//

import Foundation
import shared

class LimeSurveyViewModel: ObservableObject {
    private let coreViewModel = CoreLimeSurveyViewModel(observationFactory: AppDelegate.observationFactory)
    
    @Published var limeSurveyLink: URL?
    @Published var dataLoading = false
    @Published var wasAnswered = false
    
    init(scheduleId: String) {
        coreViewModel.setScheduleId(scheduleId: scheduleId)
        coreViewModel.onLimeSurveyLinkChange { [weak self] link in
            print("Link: \(String(describing: link))")
            DispatchQueue.main.async {
                if let link {
                    self?.limeSurveyLink = URL(string: link)
                } else {
                    self?.limeSurveyLink = nil
                }
            }
        }
        coreViewModel.onDataLoadingChange { [weak self] boolean in
            DispatchQueue.main.async {
                self?.dataLoading = boolean.boolValue
            }
        }
    }
    
    func viewDidAppear() {
        coreViewModel.viewDidAppear()
    }
    
    func viewDidDisappear() {
        coreViewModel.viewDidDisappear()
    }

    func onFinish() {
        if wasAnswered {
            coreViewModel.finish()
        } else {
            coreViewModel.cancel()
        }
    }
    
}
