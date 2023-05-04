//
//  LeaveStudyModalStateViewModel.swift
//  More
//
//  Created by Isabella Aigner on 03.05.23.
//  Copyright © 2023 Redlink GmbH. All rights reserved.
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
