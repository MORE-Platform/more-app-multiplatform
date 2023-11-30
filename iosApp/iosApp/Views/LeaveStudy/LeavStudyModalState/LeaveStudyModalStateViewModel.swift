//
//  LeaveStudyModalStateViewModel.swift
//  More
//
//  Created by Isabella Aigner on 03.05.23.
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

class LeaveStudyModalStateViewModel: ObservableObject {
    @Published var isLeaveStudyOpen: Bool
    @Published var isLeaveStudyConfirmOpen: Bool
    
    init(isLeaveStudyOpen: Bool?, isLeaveStudyConfirmOpen: Bool?) {
        self.isLeaveStudyOpen = isLeaveStudyOpen ?? false
        self.isLeaveStudyConfirmOpen = isLeaveStudyConfirmOpen ?? false
    }
    
}
